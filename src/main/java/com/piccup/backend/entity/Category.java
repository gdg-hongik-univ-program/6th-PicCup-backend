package com.piccup.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "category",
        // 같은 유저(user_id)는 똑같은 이름(name)의 카테고리를 중복해서 만들 수 없다.
        uniqueConstraints = @UniqueConstraint(
                name = "uq_category_user_name",
                columnNames = {"user_id", "name"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault; // true면 미분류

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // 소프트 삭제 여부

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 자동 실행 메서드
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 정적 팩토리 메서드
    public static Category createCategory(User user, String name, boolean isDefault) {
        Category category = new Category();
        category.user = user;
        category.name = name;
        category.isDefault = isDefault;
        return category;
    }

    public void updateName(String name) {
        this.name = name;
    }
}