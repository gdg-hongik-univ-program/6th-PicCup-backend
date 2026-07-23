package com.piccup.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "best_pick")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // 빌더용 모든 필드 생성자 닫음
@Builder(access = AccessLevel.PRIVATE)             // 빌더 접근 제어자 닫음
public class BestPick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 카테고리는 필수값
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // 유저는 6주차 전까지 임시로 비워둘 수 있도록 nullable = true 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Column(name = "candidate_count", nullable = false)
    private Integer candidateCount;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // 소프트삭제. null이면 살아있음

    @Column(name = "captured_date", nullable = false)
    private LocalDate capturedDate; // 캘린더에 꽂을 기준 날짜

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 시간까지 반영

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 정적 팩토리 메서드 (빌더 패턴)
    public static BestPick createBestPick(User user, Category category, String s3Key, LocalDate capturedDate, Integer candidateCount) {
        return BestPick.builder()
                .user(user)
                .category(category)
                .s3Key(s3Key)
                .capturedDate(capturedDate)
                .candidateCount(candidateCount)
                .build();
    }
}