package com.piccup.backend.controller;

import com.piccup.backend.dto.CategoryRequest;
import com.piccup.backend.dto.CategoryResponse;
import com.piccup.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 목록 조회", description = "미삭제 카테고리만, 커버/장수/최근날짜 포함")
    @GetMapping
    public ResponseEntity<List<CategoryResponse.Get>> getCategories(
            @SessionAttribute(name = "LOGIN_USER_ID") Long userId) {
        return ResponseEntity.ok(categoryService.getCategories(userId));
    }

    @Operation(summary = "카테고리 생성", description = "이름 중복 시 409 CATEGORY_DUPLICATE")
    @PostMapping
    public ResponseEntity<CategoryResponse.Get> createCategory(
            @SessionAttribute(name = "LOGIN_USER_ID") Long userId,
            @Valid @RequestBody CategoryRequest.Create request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(userId, request));
    }

    @Operation(summary = "카테고리 이름 수정", description = "미분류는 403 CATEGORY_PROTECTED")// 미분류 카테고리 삭제 시 삭제할 것
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse.Update> updateCategory(
            @SessionAttribute(name = "LOGIN_USER_ID") Long userId,
            @PathVariable("id") Long categoryId,
            @Valid @RequestBody CategoryRequest.Update request) {
        return ResponseEntity.ok(categoryService.updateCategory(userId, categoryId, request));
    }

    @Operation(summary = "카테고리 삭제", description = "소프트삭제 + 하위 best_pick cascade 트래시 이동. 미분류는 403")// 미분류 카테고리 삭제 시 삭제할 것
    @DeleteMapping("/{id}")
    public ResponseEntity<CategoryResponse.Delete> deleteCategory(
            @SessionAttribute(name = "LOGIN_USER_ID") Long userId,
            @PathVariable("id") Long categoryId) {
        return ResponseEntity.ok(categoryService.deleteCategory(userId, categoryId));
    }

    @Operation(summary = "카테고리 되돌리기", description = "삭제 배치에 딸린 best_pick만 원위치 복구")
    @PostMapping("/{id}/restore")
    public ResponseEntity<CategoryResponse.Restore> restoreCategory(
            @SessionAttribute(name = "LOGIN_USER_ID") Long userId,
            @PathVariable("id") Long categoryId) {
        return ResponseEntity.ok(categoryService.restoreCategory(userId, categoryId));
    }
}