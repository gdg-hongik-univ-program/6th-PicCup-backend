package com.piccup.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class UserResponse {
    @Getter
    @AllArgsConstructor
    public static class Signup {
        private Long id;
        private String email;
        private String nickname;
    }

    @Getter
    @AllArgsConstructor
    public static class Login {
        private Long id;
        private String nickname;
    }

    @Getter
    @AllArgsConstructor
    public static class Success {
        private boolean ok;
    }

    // 내 정보 조회 응답
    public record MyInfo(
            Long id,
            String email,
            String nickname,
            String profileImageUrl
    ) {}

    // 닉네임 수정 응답
    public record UpdateNickname(
            Long id,
            String nickname
    ) {}

    // 프로필 사진 수정 응답
    public record ProfileImage(
            String profileImageUrl
    ) {}
}