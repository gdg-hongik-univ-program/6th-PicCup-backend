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
}