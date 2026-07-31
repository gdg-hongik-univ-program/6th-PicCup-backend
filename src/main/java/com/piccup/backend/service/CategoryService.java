package com.piccup.backend.service;

import com.piccup.backend.dto.CategoryRequest;
import com.piccup.backend.dto.CategoryResponse;
import com.piccup.backend.entity.Category;
import com.piccup.backend.entity.User;
import com.piccup.backend.repository.CategoryRepository.CategoryListProjection;
import com.piccup.backend.repository.CategoryRepository;
import com.piccup.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;

    // 카테고리 목록 조회
    public List<CategoryResponse.Get> getCategories(Long userId) {
        List<CategoryListProjection> projections = categoryRepository.findCategoryListByUserId(userId);

        // 프로젝션을 사용, DB에서 해당 유저(userId)의 카테고리 목록과 통계 데이터를 가져옴
        return projections.stream().map(p -> { // 가공해서 프론트엔드용 상자에 옮겨 담기
            // S3 이미지 URL 생성 (카테고리에 사진이 있는 경우만)
            String coverUrl = p.getCoverImageKey() != null ?
                    s3Uploader.generatePresignedUrl(p.getCoverImageKey()) : null;

            // Response DTO 조립
            return new CategoryResponse.Get(
                    p.getId(),
                    p.getName(),
                    p.getBestPickCount() != null ? p.getBestPickCount() : 0,
                    p.getLatestCapturedDate(),
                    coverUrl,
                    p.getIsDefault() != null && p.getIsDefault()
            );
        }).collect(Collectors.toList()); // 변환이 끝난 객체들을 다시 하나의 리스트로 포장해 컨트롤러로 넘겨줍
    }

    // 카테고리 생성
    @Transactional
    public CategoryResponse.Get createCategory(Long userId, CategoryRequest.Create request) {
        // 카테고리 개인화(유저 매핑)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        // 해당 유저가 이미 똑같은 이름(으로 만들어둔 카테고리가 있는지 검사(삭제된 것 제외)
        if (categoryRepository.existsByUserIdAndNameAndDeletedAtIsNull(userId, request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CATEGORY_DUPLICATE");
        }

        // 새 카테고리 객체 만듬 (사용자가 만드는 것이니 isDefault는 false)
        Category category = Category.createCategory(user, request.name(), false);
        // JPA에게 이 객체를 DB에 Insert 하라고 시킴
        Category saved = categoryRepository.save(category);

        // 새로 만든 카테고리 초기 세팅 후 컨트롤러로 넘겨줌
        return new CategoryResponse.Get(
                saved.getId(),
                saved.getName(),
                0,
                null,
                null,
                saved.isDefault()
        );
    }

    // 카테고리 이름 수정
    @Transactional
    public CategoryResponse.Update updateCategory(Long userId, Long categoryId, CategoryRequest.Update request) {
        // 바꾸고자 하는 카테고리가 DB에 있는지 그리고 삭제된 건 아닌지 찾음
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND"));

        // 로그인한 유저와 방금 DB에서 꺼낸 카테고리 주인의 ID가 같은지 비교 (보안)
        if (!category.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN_RESOURCE");
        }

        // 이 카테고리가 시스템이 만든 미분류 카테고리라면, 이름 수정을 금지
        if (category.isDefault()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "CATEGORY_PROTECTED");
        }

        // 기존 이름과 새로 바꾸려는 이름이 다를 때만 중복 검사
        if (!category.getName().equals(request.name()) &&
                categoryRepository.existsByUserIdAndNameAndDeletedAtIsNull(userId, request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CATEGORY_DUPLICATE");
        }

        // 엔티티의 이름을 새 이름으로 바꿈
        category.updateName(request.name());
        // 수정된 ID와 이름을 DTO에 담아 컨트롤러에 반환
        return new CategoryResponse.Update(category.getId(), category.getName());
    }
}