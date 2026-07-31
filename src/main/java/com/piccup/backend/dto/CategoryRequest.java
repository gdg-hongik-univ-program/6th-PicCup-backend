package com.piccup.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryRequest {

    // 카테고리 생성 요청
    public record Create(
            @NotBlank(message = "VALIDATION_ERROR")
            @Size(max = 50, message = "VALIDATION_ERROR")
            String name
    ) {}

    // 카테고리 수정 요청 (Create와 필드가 같음, 치후 수정 가능)
    public record Update(
            @NotBlank(message = "VALIDATION_ERROR")
            @Size(max = 50, message = "VALIDATION_ERROR")
            String name
    ) {}
}