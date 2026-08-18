package com.piccup.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryRequest {

    public record Create(
            @NotBlank(message = "카테고리 이름은 필수입니다.")
            @Size(min = 1, max = 50, message = "카테고리 이름은 1자 이상 50자 이내여야 합니다.")
            String name
    ) {}

    public record Update(
            @NotBlank(message = "카테고리 이름은 필수입니다.")
            @Size(min = 1, max = 50, message = "카테고리 이름은 1자 이상 50자 이내여야 합니다.")
            String name
    ) {}
}