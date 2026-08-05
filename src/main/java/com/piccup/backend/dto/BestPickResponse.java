package com.piccup.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
            String imageUrl
    ) {}
}