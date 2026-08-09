package com.piccup.backend.dto;

import java.util.List;

public class BestPickRequest {

    // 카테고리 다중 이동 요청
    public record MoveCategory(
            List<Long> ids,
            Long targetCategoryId
    ) {}

    // 좋아요 상태 변경 요청
    public record UpdateLike(
            boolean isLiked
    ) {}
}