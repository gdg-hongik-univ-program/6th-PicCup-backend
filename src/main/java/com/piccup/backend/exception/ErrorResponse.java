package com.piccup.backend.exception;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {

    private String code;
    private String message;

    @Builder
    private ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    // 일반 비즈니스 에러 응답
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    // @Valid 유효성 검사 에러 전용 응답 (오류 위치 상세)
    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        StringBuilder sb = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append("[");
            sb.append(fieldError.getField());
            sb.append("](은)는 ");
            sb.append(fieldError.getDefaultMessage());
            sb.append(" ");
        }

        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(sb.toString().trim())
                .build();
    }
}