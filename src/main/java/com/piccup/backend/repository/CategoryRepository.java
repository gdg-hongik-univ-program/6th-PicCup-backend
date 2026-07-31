package com.piccup.backend.repository;

import com.piccup.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 네이티브 쿼리 결과를 담는 임시 중간 바구니라고 생각
    interface CategoryListProjection {
        Long getId();
        String getName();
        Integer getBestPickCount();
        LocalDate getLatestCapturedDate();
        String getCoverImageKey();
        Boolean getIsDefault();
    }

    //특정 유저의 카테고리 목록 화면을    그리기 위해 필요한 모든 데이터 조회
    @Query(value = """
        SELECT
            c.id AS id,
            c.name AS name,
            c.is_default AS isDefault,
            COUNT(bp.id) AS bestPickCount,
            MAX(bp.captured_date) AS latestCapturedDate,
            (SELECT bp2.s3_key FROM best_pick bp2 
             WHERE bp2.category_id = c.id AND bp2.deleted_at IS NULL 
             ORDER BY bp2.captured_date DESC, bp2.id DESC LIMIT 1) AS coverImageKey
        FROM category c
        LEFT JOIN best_pick bp ON c.id = bp.category_id AND bp.deleted_at IS NULL
        WHERE c.user_id = :userId AND c.deleted_at IS NULL
        GROUP BY c.id
        ORDER BY c.created_at ASC
    """, nativeQuery = true)
    List<CategoryListProjection> findCategoryListByUserId(@Param("userId") Long userId);

    // 해당 유저 ID와 이름을 가졌으면서, 삭제되지 않은 카테고리 (중복 검사용)
    boolean existsByUserIdAndNameAndDeletedAtIsNull(Long userId, String name);

    // ID로 찾되, 삭제되지 않은 카테고리 (수정/삭제 전 조회용)
    Optional<Category> findByIdAndDeletedAtIsNull(Long id);
}