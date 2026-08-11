package com.piccup.backend.service;

import com.piccup.backend.dto.BestPickResponse;
import com.piccup.backend.entity.BestPick;
import com.piccup.backend.entity.Category;
import com.piccup.backend.entity.User;
import com.piccup.backend.repository.BestPickRepository;
import com.piccup.backend.repository.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BestPickTrashService {

    private static final int RETENTION_DAYS = 30;
    private static final String UNCATEGORIZED = "분류 전";

    private final BestPickRepository bestPickRepository;
    private final CategoryRepository categoryRepository;
    private final S3Uploader s3Uploader;

    // 클래스 레벨 @Transactional을 붙이지 않는다.
    public BestPickResponse.Delete softDelete(Long userId, List<Long> ids) {
        List<BestPick> targets = bestPickRepository.findAliveByIdsAndUserId(ids, userId);
        if (targets.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "BEST_PICK_NOT_FOUND");
        }

        // 1. S3 복사 (원본은 아직 남겨둔다)
        List<String> originKeys = targets.stream().map(BestPick::getS3Key).toList();
        List<String> trashKeys = originKeys.stream().map(s3Uploader::copyToTrash).toList();

        // 2. DB 갱신 — 배치 시각은 초 단위로 통일 (카테고리 삭제와 동일 규칙)
        LocalDateTime batchTime = LocalDateTime.now().withNano(0);
        for (int i = 0; i < targets.size(); i++) {
            targets.get(i).moveToTrash(trashKeys.get(i), batchTime);
        }
        bestPickRepository.saveAll(targets);   // 트랜잭션 없이 엔티티만 고치면 flush 안 됨

        // 3. 원본 삭제 (여기서 실패해도 정합성엔 영향 없음)
        originKeys.forEach(s3Uploader::deleteQuietly);

        return new BestPickResponse.Delete(ids);
    }

    @Transactional(readOnly = true)
    public List<BestPickResponse.Trash> getTrash(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<BestPick> picks = bestPickRepository.findTrash(userId, now.minusDays(RETENTION_DAYS));

        return picks.stream()
                .map(pick -> new BestPickResponse.Trash(
                        pick.getId(),
                        pick.getDeletedAt(),
                        calculateDaysLeft(pick.getDeletedAt(), now),
                        s3Uploader.generatePresignedUrl(pick.getS3Key())
                ))
                .toList();
    }

    public BestPickResponse.Restore restore(Long userId, List<Long> ids) {
        List<BestPick> targets = bestPickRepository.findTrashedByIdsAndUserId(ids, userId);
        if (targets.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "BEST_PICK_NOT_FOUND");
        }

        RestoreContext context = new RestoreContext();
        List<BestPick> succeeded = new ArrayList<>();
        List<String> trashKeysToDelete = new ArrayList<>();
        List<BestPickResponse.RestoreItem> restored = new ArrayList<>();
        List<Long> skipped = new ArrayList<>();

        for (BestPick pick : targets) {
            String trashKey = pick.getS3Key();

            // Lifecycle(30일)로 이미 사라진 객체 방어
            if (!s3Uploader.exists(trashKey)) {
                log.warn("복구 대상 S3 객체 없음 (만료 추정): pickId={}, key={}", pick.getId(), trashKey);
                skipped.add(pick.getId());
                continue;
            }

            String originalKey = s3Uploader.copyToOriginal(trashKey);   // 복사만
            Category destination = resolveDestination(pick, context);

            pick.changeCategory(destination);
            pick.restore(originalKey);

            succeeded.add(pick);
            trashKeysToDelete.add(trashKey);        // restore() 후엔 역산 불가라 미리 모은다
            restored.add(new BestPickResponse.RestoreItem(
                    pick.getId(), destination.getId(), destination.getName()));
        }

        bestPickRepository.saveAll(succeeded);      // 트랜잭션 없으니 명시 호출 필수
        trashKeysToDelete.forEach(s3Uploader::deleteQuietly);

        return new BestPickResponse.Restore(restored, skipped);
    }

    //영구삭제. DB row를 먼저 지우고 S3 객체를 지운다.
    public BestPickResponse.Purge purge(Long userId, List<Long> ids) {
        List<BestPick> targets = bestPickRepository.findTrashedByIdsAndUserId(ids, userId);
        if (targets.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "BEST_PICK_NOT_FOUND");
        }

        List<String> keys = targets.stream().map(BestPick::getS3Key).toList();

        bestPickRepository.deleteAllInBatch(targets);   // DB 먼저
        keys.forEach(s3Uploader::deleteQuietly);        // S3 나중, 실패해도 로그만

        return new BestPickResponse.Purge(ids);
    }

    private long calculateDaysLeft(LocalDateTime deletedAt, LocalDateTime now) {
        long elapsed = ChronoUnit.DAYS.between(deletedAt, now);
        return Math.max(0, RETENTION_DAYS - elapsed);
    }

    private Category resolveDestination(BestPick pick, RestoreContext context) {
        Category origin = pick.getCategory();

        Category cached = context.byOriginId.get(origin.getId());
        if (cached != null) {
            return cached;
        }

        Long userId = pick.getUser().getId();
        Category destination;

        if (origin.getDeletedAt() == null) {
            // 케이스 1 — 원 카테고리 살아있음
            destination = origin;

        } else if (!categoryRepository.existsByUserIdAndNameAndDeletedAtIsNull(userId, origin.getName())) {
            // 케이스 2 — 동명 활성 카테고리 없음 → 원 카테고리 부활
            origin.restore();
            destination = categoryRepository.save(origin);

        } else {
            // 케이스 3 — 이름 충돌 → 분류 전
            destination = resolveUncategorized(pick.getUser(), context);
        }

        context.byOriginId.put(origin.getId(), destination);
        return destination;
    }

    private Category resolveUncategorized(User user, RestoreContext context) {
        if (context.uncategorized != null) {
            return context.uncategorized;                       // 배치 내 중복 생성 방지
        }
        Category target = categoryRepository
                .findByUserIdAndNameAndDeletedAtIsNull(user.getId(), UNCATEGORIZED)
                .orElseGet(() -> categoryRepository.save(           // 기존 것 있으면 재사용
                        Category.createCategory(user, UNCATEGORIZED, false)));
        context.uncategorized = target;
        return target;
    }
    //복구 배치 1회 동안의 카테고리 해석 결과 
    private static final class RestoreContext {
        private final Map<Long, Category> byOriginId = new HashMap<>();
        private Category uncategorized;   // 케이스 3 전용 단일 슬롯
    }
}
