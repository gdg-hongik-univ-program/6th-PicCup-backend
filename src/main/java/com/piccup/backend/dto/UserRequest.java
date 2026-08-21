package com.piccup.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public class UserRequest {

    @Getter
    public static class Signup {
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 1, max = 10, message = "닉네임은 1자 이상, 10자 이내여야 합니다.")
        private String nickname;

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{10,16}$",
                message = "비밀번호는 10~16자의 영문, 숫자를 포함해야 합니다.")
        private String password;
    }

    @Getter
    public static class Login {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;
    }

    @Getter
    public static class PasswordReset {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{10,16}$",
                message = "비밀번호는 10~16자의 영문, 숫자를 포함해야 합니다.")
        private String newPassword;
    }

    @Getter
    public static class UpdateNickname {
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 1, max = 10, message = "닉네임은 1자 이상, 10자 이내여야 합니다.")
        private String nickname;
    }
}