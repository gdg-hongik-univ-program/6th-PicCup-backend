package com.piccup.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "best_pick")
@Getter
@Setter
@NoArgsConstructor
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

    @Column(name = "image_url")
    private String imageUrl; // S3 또는 임시 로컬 경로

    @Column(name = "captured_date")
    private LocalDate capturedDate; // 캘린더에 꽂을 기준 날짜

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 시간까지 반영

    // DB에 데이터가 처음 INSERT 되기 직전에 현재 시간을 자동으로 세팅
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}