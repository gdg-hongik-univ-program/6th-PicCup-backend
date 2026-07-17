package com.piccup.backend.dto;

import lombok.Getter;

public class UserRequest {

    @Getter
    public static class Signup {
        private String nickname; // 1~10자
        private String email;
        private String password; // 10~16자 영문+숫자
    }

    @Getter
    public static class Login {
        private String email;
        private String password;
    }

    @Getter
    public static class PasswordReset {
        private String email; // 이메일만 알면 리셋 가능
        private String newPassword;
    }
}