package com.piccup.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "category",
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

    // 6주차 전까지 nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 정적 팩토리 메서드
    public static Category createCategory(User user, String name) {
        Category category = new Category();
        category.user = user;
        category.name = name;
        return category;
    }
}