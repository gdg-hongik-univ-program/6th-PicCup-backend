package com.piccup.backend.controller;

import com.piccup.backend.dto.CategoryRequest;
import com.piccup.backend.dto.CategoryResponse;
import com.piccup.backend.service.CategoryService;
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

    @GetMapping
    public ResponseEntity<List<CategoryResponse.Get>> getCategories(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(categoryService.getCategories(userId));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse.Get> createCategory(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CategoryRequest.Create request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse.Update> updateCategory(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("id") Long categoryId,
            @Valid @RequestBody CategoryRequest.Update request) {
        return ResponseEntity.ok(categoryService.updateCategory(userId, categoryId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CategoryResponse.Delete> deleteCategory(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("id") Long categoryId) {
        return ResponseEntity.ok(categoryService.deleteCategory(userId, categoryId));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<CategoryResponse.Restore> restoreCategory(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("id") Long categoryId) {
        return ResponseEntity.ok(categoryService.restoreCategory(userId, categoryId));
    }
}