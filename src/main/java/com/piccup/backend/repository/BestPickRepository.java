package com.piccup.backend.repository;

import com.piccup.backend.entity.BestPick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BestPickRepository extends JpaRepository<BestPick, Long> {

    // 카테고리 삭제 cascade용: 해당 카테고리의 살아있는 픽 전부
    List<BestPick> findByCategoryIdAndDeletedAtIsNull(Long categoryId);

    // 카테고리 되돌리기용
    List<BestPick> findByCategoryIdAndDeletedAt(Long categoryId, LocalDateTime deletedAt);

    // 월간 캘린더 조회 (카테고리 이름 포함 Fetch Join)
    @Query("SELECT bp FROM BestPick bp JOIN FETCH bp.category c " +
            "WHERE bp.user.id = :userId AND bp.deletedAt IS NULL " +
            "AND bp.capturedDate >= :startDate AND bp.capturedDate < :endDate " +
            "ORDER BY bp.capturedDate ASC, bp.createdAt ASC")
    List<BestPick> findCalendarPicks(@Param("userId") Long userId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    // 단건 상세 조회 (카테고리 이름 포함 Fetch Join)
    @Query("SELECT bp FROM BestPick bp JOIN FETCH bp.category c " +
            "WHERE bp.id = :id AND bp.deletedAt IS NULL")
    Optional<BestPick> findByIdWithCategory(@Param("id") Long id);

    // 조건 없이 전체 조회 (categoryId 쿼리스트링이 없을 때)
    @Query("SELECT bp FROM BestPick bp JOIN FETCH bp.category c " +
            "WHERE bp.user.id = :userId AND bp.deletedAt IS NULL " +
            "ORDER BY bp.createdAt DESC")
    List<BestPick> findAllByUserId(@Param("userId") Long userId);

    // 특정 카테고리 앨범 조회
    @Query("SELECT bp FROM BestPick bp JOIN FETCH bp.category c " +
            "WHERE bp.user.id = :userId AND bp.category.id = :categoryId AND bp.deletedAt IS NULL " +
            "ORDER BY bp.createdAt DESC")
    List<BestPick> findAllByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    // 다중 이동용: 유저 소유의 특정 사진 여러 개 조회 (삭제되지 않은 것만)
    List<BestPick> findByIdInAndUserIdAndDeletedAtIsNull(List<Long> ids, Long userId);

    @Query("select bp from BestPick bp " +
            "where bp.id in :ids and bp.user.id = :userId and bp.deletedAt is null")
    List<BestPick> findAliveByIdsAndUserId(@Param("ids") List<Long> ids,
                                           @Param("userId") Long userId);

    @Query("select bp from BestPick bp " +
            "where bp.user.id = :userId and bp.deletedAt > :threshold " +
            "order by bp.deletedAt desc")
    List<BestPick> findTrash(@Param("userId") Long userId,
                             @Param("threshold") LocalDateTime threshold);
}