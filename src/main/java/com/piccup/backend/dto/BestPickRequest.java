package com.piccup.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BestPickRequest {

    public record MoveCategory(
            @NotEmpty(message = "이동할 사진 ID 목록은 비어 있을 수 없습니다.")
            List<@NotNull(message = "사진 ID는 null일 수 없습니다.") Long> ids, // 이 부분!

            @NotNull(message = "목표 카테고리 ID는 필수입니다.")
            Long targetCategoryId
    ) {}

    public record UpdateLike(
            @NotNull(message = "좋아요 상태값은 필수입니다.")
            Boolean isLiked
    ) {}

    public record Ids(
            @NotEmpty(message = "사진 ID 목록은 비어 있을 수 없습니다.")
            List<@NotNull(message = "사진 ID는 null일 수 없습니다.") Long> ids
    ) {}
}