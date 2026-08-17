package com.piccup.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public class CategoryResponse {

    // 목록 조회 및 생성 시 사용
    public record Get(
            Long id,
            String name,
            int bestPickCount,
            LocalDate latestCapturedDate,
            String coverImageUrl,
            // 미분류 카테고리 삭제 시 삭제할 것
            @JsonProperty("isDefault") boolean isDefault //이름 강제변환 방지
    ) {}

    // 카테고리 이름 수정 시 사용
    public record Update(
            Long id,
            String name
    ) {}

    // 카테고리 삭제 시 사용
    public record Delete(Long id, int deletedBestPickCount) {}

    // 카테고리 삭제 후 되돌리기 시 사용
    public record Restore(Long id, int restoredBestPickCount) {}
}