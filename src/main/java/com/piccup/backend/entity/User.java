package com.piccup.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users") // 예약어 중복 방지
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 접근 제어
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(length = 10)
    private String nickname;

    @Column(name = "profile_image_s3_key", length = 512)
    private String profileImageS3Key;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 정적 팩토리 메서드
    public static User createUser(String email, String passwordHash, String nickname, String profileImageS3Key) {
        User user = new User();
        user.email = email;
        user.passwordHash = passwordHash;
        user.nickname = nickname;
        user.profileImageS3Key = profileImageS3Key;
        return user;
    }

    // 비밀번호 재설정용 메서드
    public void updatePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    // 닉네임 수정 메서드
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // 프로필 이미지 키 갱신 메서드
    public void updateProfileImage(String s3Key) {
        this.profileImageS3Key = s3Key;
    }
}