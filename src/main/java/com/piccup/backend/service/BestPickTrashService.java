package com.piccup.backend.service;

import com.piccup.backend.dto.BestPickResponse;
import com.piccup.backend.entity.BestPick;
import com.piccup.backend.repository.BestPickRepository;
import com.piccup.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BestPickTrashService {

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
}
