package com.piccup.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BestPickResponse(
        Long id,
        Long categoryId,
        LocalDate capturedDate,
        int candidateCount,
        LocalDateTime createdAt,
        String imageUrl        // presigned GET URL — DB엔 저장 안 함
) {}
