package com.piccup.backend.repository;

import com.piccup.backend.entity.BestPick;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BestPickRepository extends JpaRepository<BestPick, Long> {

    // 카테고리 삭제 cascade용: 해당 카테고리의 살아있는 픽 전부
    List<BestPick> findByCategoryIdAndDeletedAtIsNull(Long categoryId);

    // 카테고리 되돌리기용
    List<BestPick> findByCategoryIdAndDeletedAt(Long categoryId, LocalDateTime deletedAt);
}