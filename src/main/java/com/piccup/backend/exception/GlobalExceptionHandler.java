package com.piccup.backend.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 직접 정의한 비즈니스 로직 에러 처리
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("BusinessException: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = ErrorResponse.of(errorCode);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // @Valid 유효성 검사 실패 에러 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("Validation Error: {}", e.getMessage());
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        // ErrorResponse 안에 만들어둔 BindingResult 파싱 메서드 호출
        ErrorResponse response = ErrorResponse.of(errorCode, e.getBindingResult());
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // @Validated 에러 처리 (@Min, @PastOrPresent 등)
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        log.warn("ConstraintViolation: {}", e.getMessage());
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        ErrorResponse response = ErrorResponse.of(errorCode);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // 타입 불일치 에러 (abc를 숫자에 넣을 때, 날짜 포맷이 틀렸을 때 등)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("TypeMismatch: {}", e.getMessage());
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        ErrorResponse response = ErrorResponse.of(errorCode);
        // 커스텀 메시지를 던지고 싶다면 ErrorResponse 빌더를 직접 사용할 수도 있습니다.
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    //잘못된 JSON 요청 (Body JSON 형식이 깨졌을 때)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadable: {}", e.getMessage());
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        ErrorResponse response = ErrorResponse.of(errorCode);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // 지원하지 않는 HTTP 메서드 호출 (405)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("HttpRequestMethodNotSupported: {}", e.getMessage());
        // 405는 클라이언트 잘못(400번대)이므로 포맷 유지를 위해 임의로 VALIDATION_ERROR로 묶거나 별도 코드 생성 가능
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        ErrorResponse response = ErrorResponse.of(errorCode);
        return ResponseEntity.status(e.getStatusCode()).body(response);
    }

    // 잘못된 URL 경로 호출 (404) - 스프링 3.2+ 정적 리소스 매핑 우회 시 발생
    @ExceptionHandler(NoResourceFoundException.class)
    protected ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("NoResourceFound: {}", e.getMessage());
        // 프론트엔드 포맷 통일용 에러 반환
        return ResponseEntity.status(404)
                .body(ErrorResponse.builder().code("NOT_FOUND").message("존재하지 않는 API 경로입니다.").build());
    }

    // 그 외의 예상치 못한 서버 에러 (500)
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled Exception 발생: ", e);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        ErrorResponse response = ErrorResponse.of(errorCode);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }
}