package com.piccup.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BestPickResponse {

    // 사진 업로드 성공 시 반환
    public record Upload(
            Long id,
            Long categoryId,
            LocalDate capturedDate,
            int candidateCount,
            LocalDateTime createdAt,
            String imageUrl
    ) {}

    // 홈 캘린더 월간 조회 시 반환 (candidateCount 없음)
    public record Calendar(
            Long id,
            Long categoryId,
            String categoryName,
            LocalDate capturedDate,
            LocalDateTime createdAt,
            String imageUrl
    ) {}

    // 상세 조회 시 반환 (categoryName 포함, candidateCount 포함)
    public record Detail(
            Long id,
            Long categoryId,
            String categoryName,
            LocalDate capturedDate,
            int candidateCount,
            LocalDateTime createdAt,
            String imageUrl,
            boolean isLiked
    ) {}

    // 카테고리별 사진 조회(앨범)용 응답
    public record Album(
            Long id,
            Long categoryId,
            String categoryName,
            LocalDate capturedDate,
            LocalDateTime createdAt,
            String imageUrl,
            boolean isLiked
    ) {}

    // 카테고리 다중 이동 성공 시 응답
    public record MoveResult(
            List<Long> movedIds,
            Long categoryId,
            String categoryName
    ) {}

    // 좋아요 변경 성공 시 응답
    public record LikeResult(
            Long id,
            Long categoryId,
            String categoryName,
            boolean isLiked
    ) {}

    public record Delete(List<Long> deleted) {}

    public record Trash(Long id, LocalDateTime deletedAt, long daysLeft, String imageUrl) {}

    public record RestoreItem(Long id, Long categoryId, String categoryName) {}

    public record Restore(List<RestoreItem> restored, List<Long> skipped) {}

    public record Purge(List<Long> purged) {}
}