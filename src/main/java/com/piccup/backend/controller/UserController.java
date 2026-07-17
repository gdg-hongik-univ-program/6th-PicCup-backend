package com.piccup.backend.controller;

import com.piccup.backend.dto.UserRequest;
import com.piccup.backend.dto.UserResponse;
import com.piccup.backend.entity.User;
import com.piccup.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private static final String LOGIN_USER = "LOGIN_USER_ID";

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserResponse.Signup> signup(@RequestBody UserRequest.Signup request) {
        User savedUser = userService.signup(request);

        UserResponse.Signup response = new UserResponse.Signup(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname()
        );

        // 성공 시 201 Created 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<UserResponse.Login> login(@RequestBody UserRequest.Login request, HttpServletRequest httpRequest) {
        // 1. 서비스에 아이디/비번 검증을 맡김
        User loginUser = userService.login(request);
        // 2. 세션 공간 할당
        HttpSession session = httpRequest.getSession(true);
        // 3. 세션 메모리에 유저 정보 저장
        session.setAttribute(LOGIN_USER, loginUser.getId()); // 내부적으로 JDBC 드라이버를 통해 Aiven MySQL로 UPDATE 쿼리 전송

        UserResponse.Login response = new UserResponse.Login(loginUser.getId(), loginUser.getNickname());

        // 200 OK 반환
        return ResponseEntity.ok(response);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<UserResponse.Success> logout(HttpServletRequest httpRequest) {
        // 기존에 할당된 세션이 있는지 확인, 없다면 새로 만들지 않고 null을 반환
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate(); // 해당 세션 데이터를 MySQL DB에서 완전히 삭제
        }

        // Response Body (200) { "ok": true } 반환
        return ResponseEntity.ok(new UserResponse.Success(true));
    }

    // 비밀번호 재설정
    @PostMapping("/password/reset")
    public ResponseEntity<UserResponse.Success> resetPassword(@RequestBody UserRequest.PasswordReset request) {
        userService.resetPassword(request);

        return ResponseEntity.ok(new UserResponse.Success(true));
    }
}