package com.piccup.backend.service;

import com.piccup.backend.dto.BestPickResponse;
import com.piccup.backend.entity.BestPick;
import com.piccup.backend.entity.Category;
import com.piccup.backend.repository.BestPickRepository;
import com.piccup.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BestPickService {

    private final BestPickRepository bestPickRepository;
    private final CategoryRepository categoryRepository;
    private final S3Uploader s3Uploader;

    public BestPickResponse upload(Long userId, MultipartFile file,
                                   Long categoryId, LocalDate capturedDate, int candidateCount) {

        // 1. 파일 검증 (S3 올리기 전에)
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("INVALID_IMAGE"); // TODO: 커스텀 예외
        }

        // 2. 카테고리 소유권 검증 (IDOR) — S3 업로드 '전에'
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("CATEGORY_NOT_FOUND"));
        // getUser().getId() 는 LAZY 프록시여도 FK만 읽어서 추가 쿼리 안 나간다
        if (!userId.equals(category.getUser().getId())) {
            throw new IllegalArgumentException("FORBIDDEN_RESOURCE");
        }

        // 3. S3 업로드 (key 생성 포함, key 리턴)
        String key = s3Uploader.upload(file, userId);

        // 4. DB insert, 실패 시 3에서 S3에 업로드한 키 삭제
        try {
            // 정적 팩토리 메서드 사용
            BestPick saved = bestPickRepository.save(
                    BestPick.createBestPick(
                            category.getUser(),
                            category,
                            key,
                            capturedDate,
                            candidateCount
                    )
            );
            return toResponse(saved);
        } catch (RuntimeException e) {
            s3Uploader.delete(key);  // 보상 삭제
            throw e;
        }
    }

    private BestPickResponse toResponse(BestPick bp) {
        return new BestPickResponse(
                bp.getId(),
                bp.getCategory().getId(),
                bp.getCapturedDate(),
                bp.getCandidateCount(),
                bp.getCreatedAt(),
                s3Uploader.generatePresignedUrl(bp.getS3Key())
        );
    }
}
