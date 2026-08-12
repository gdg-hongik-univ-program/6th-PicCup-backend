package com.piccup.backend.dto;

import lombok.Getter;

public class UserRequest {
    @Getter
    public static class Signup {
        private String nickname;
        private String email;
        private String password;
    }

    @Getter
    public static class Login {
        private String email;
        private String password;
    }

    @Getter
    public static class PasswordReset {
        private String email;
        private String newPassword;
    }

    @Getter
    public static class UpdateNickname {
        private String nickname;
    }
}