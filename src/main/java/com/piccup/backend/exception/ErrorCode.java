package com.piccup.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 400 BAD_REQUEST: 클라이언트의 잘못된 요청
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "필드 검증에 실패했습니다."),
    INVALID_IMAGE(HttpStatus.BAD_REQUEST, "INVALID_IMAGE", "지원하지 않는 이미지 형식이거나 파일이 없습니다."),
    NO_IMAGE_PROVIDED(HttpStatus.BAD_REQUEST, "NO_IMAGE_PROVIDED", "업로드할 이미지가 제공되지 않았습니다."),
    CATEGORY_NOT_DELETED(HttpStatus.BAD_REQUEST, "CATEGORY_NOT_DELETED", "삭제되지 않은 카테고리는 복구할 수 없습니다."),

    // 401 UNAUTHORIZED: 인증되지 않은 사용자
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "로그인이 필요하거나 세션이 만료되었습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "LOGIN_FAILED", "이메일 또는 비밀번호가 일치하지 않습니다."),

    // 403 FORBIDDEN: 권한 없음
    FORBIDDEN_RESOURCE(HttpStatus.FORBIDDEN, "FORBIDDEN_RESOURCE", "해당 리소스에 접근할 권한이 없습니다."),

    // 404 NOT_FOUND: 리소스를 찾을 수 없음
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "존재하지 않는 사용자입니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "존재하지 않는 카테고리입니다."),
    BEST_PICK_NOT_FOUND(HttpStatus.NOT_FOUND, "BEST_PICK_NOT_FOUND", "존재하지 않는 사진입니다."),

    // 409 CONFLICT: 리소스 충돌
    CATEGORY_DUPLICATE(HttpStatus.CONFLICT, "CATEGORY_DUPLICATE", "이미 존재하는 카테고리 이름입니다."),
    USER_DUPLICATE(HttpStatus.CONFLICT, "USER_DUPLICATE", "이미 가입된 이메일입니다."),

    // 500 INTERNAL_SERVER_ERROR: 서버 내부 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}