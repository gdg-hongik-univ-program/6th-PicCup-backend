package com.piccup.backend.service;

import com.piccup.backend.dto.BestPickRequest;
import com.piccup.backend.dto.BestPickResponse;
import com.piccup.backend.entity.BestPick;
import com.piccup.backend.entity.Category;
import com.piccup.backend.repository.BestPickRepository;
import com.piccup.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BestPickService {

    private final BestPickRepository bestPickRepository;
    private final CategoryRepository categoryRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public BestPickResponse.Upload upload(Long userId, MultipartFile file,
                                          Long categoryId, LocalDate capturedDate, int candidateCount) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_IMAGE");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND"));

        if (!userId.equals(category.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN_RESOURCE");
        }

        String key = s3Uploader.upload(file, userId);

        try {
            BestPick saved = bestPickRepository.save(
                    BestPick.createBestPick(
                            category.getUser(),
                            category,
                            key,
                            capturedDate,
                            candidateCount
                    )
            );
            return new BestPickResponse.Upload(
                    saved.getId(),
                    saved.getCategory().getId(),
                    saved.getCapturedDate(),
                    saved.getCandidateCount(),
                    saved.getCreatedAt(),
                    s3Uploader.generatePresignedUrl(saved.getS3Key())
            );
        } catch (RuntimeException e) {
            s3Uploader.delete(key);
            throw e;
        }
    }

    // 캘린더 데이터 조회
    public List<BestPickResponse.Calendar> getCalendar(Long userId, String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.plusMonths(1).atDay(1);

        List<BestPick> picks = bestPickRepository.findCalendarPicks(userId, startDate, endDate);

        return picks.stream().map(bp -> new BestPickResponse.Calendar(
                bp.getId(),
                bp.getCategory().getId(),
                bp.getCategory().getName(),
                bp.getCapturedDate(),
                bp.getCreatedAt(),
                s3Uploader.generatePresignedUrl(bp.getS3Key())
        )).collect(Collectors.toList());
    }

    // 사진 단건 상세 조회
    public BestPickResponse.Detail getBestPickDetail(Long userId, Long bestPickId) {
        BestPick bp = bestPickRepository.findByIdWithCategory(bestPickId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "BEST_PICK_NOT_FOUND"));

        // 보안
        if (!bp.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN_RESOURCE");
        }

        return new BestPickResponse.Detail(
                bp.getId(),
                bp.getCategory().getId(),
                bp.getCategory().getName(),
                bp.getCapturedDate(),
                bp.getCandidateCount(),
                bp.getCreatedAt(),
                s3Uploader.generatePresignedUrl(bp.getS3Key()),
                bp.isLiked()
        );
    }

    // 카테고리별 사진 조회 (앨범)
    public List<BestPickResponse.Album> getBestPicks(Long userId, Long categoryId) {
        List<BestPick> picks;

        if (categoryId == null) {
            picks = bestPickRepository.findAllByUserId(userId);
        } else {
            picks = bestPickRepository.findAllByUserIdAndCategoryId(userId, categoryId);
        }

        return picks.stream().map(bp -> new BestPickResponse.Album(
                bp.getId(),
                bp.getCategory().getId(),
                bp.getCategory().getName(),
                bp.getCapturedDate(),
                bp.getCreatedAt(),
                s3Uploader.generatePresignedUrl(bp.getS3Key()),
                bp.isLiked()
        )).collect(Collectors.toList());
    }

    // 카테고리 다중 이동
    @Transactional
    public BestPickResponse.MoveResult moveCategories(Long userId, BestPickRequest.MoveCategory request) {
        Category targetCategory = categoryRepository.findById(request.targetCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND"));

        if (!targetCategory.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN_RESOURCE");
        }

        List<BestPick> picksToMove = bestPickRepository.findByIdInAndUserIdAndDeletedAtIsNull(request.ids(), userId);

        for (BestPick pick : picksToMove) {
            pick.changeCategory(targetCategory);
        }

        List<Long> movedIds = picksToMove.stream()
                .map(BestPick::getId)
                .toList();

        return new BestPickResponse.MoveResult(
                movedIds,
                targetCategory.getId(),
                targetCategory.getName()
        );
    }

    // 베스트픽 좋아요 상태 변경
    @Transactional
    public BestPickResponse.LikeResult updateLike(Long userId, Long pickId, BestPickRequest.UpdateLike request) {
        BestPick pick = bestPickRepository.findByIdWithCategory(pickId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "BEST_PICK_NOT_FOUND"));

        if (!pick.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN_RESOURCE");
        }

        pick.changeLike(request.isLiked());

        return new BestPickResponse.LikeResult(
                pick.getId(),
                pick.getCategory().getId(),
                pick.getCategory().getName(),
                pick.isLiked()
        );
    }
}