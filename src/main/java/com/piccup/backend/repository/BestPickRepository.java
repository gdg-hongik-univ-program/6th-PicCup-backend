package com.piccup.backend.repository;

import com.piccup.backend.entity.BestPick;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BestPickRepository extends JpaRepository<BestPick, Long> {

    // 나중에 캘린더 기능을 위해 '특정 달의 데이터만 조회'하거나
    // '특정 유저의 데이터만 조회'하는 메서드를 여기에 추가하게 됩니다.
    // 예: List<BestPick> findByUserId(Long userId);
}