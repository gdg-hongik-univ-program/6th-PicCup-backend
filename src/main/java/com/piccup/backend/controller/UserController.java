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

    @PostMapping("/signup")
    public ResponseEntity<UserResponse.Signup> signup(@RequestBody UserRequest.Signup request) {
        User savedUser = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserResponse.Signup(savedUser.getId(), savedUser.getEmail(), savedUser.getNickname()));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse.Login> login(@RequestBody UserRequest.Login request, HttpServletRequest httpRequest) {
        User loginUser = userService.login(request);

        // 로그인 성공 시 세션 발급 (DB에 저장)
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(LOGIN_USER, loginUser.getId());

        return ResponseEntity.ok(new UserResponse.Login(loginUser.getId(), loginUser.getNickname()));
    }

    @PostMapping("/logout")
    public ResponseEntity<UserResponse.Success> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate(); // 세션 무효화
        }
        return ResponseEntity.ok(new UserResponse.Success(true));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<UserResponse.Success> resetPassword(@RequestBody UserRequest.PasswordReset request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(new UserResponse.Success(true));
    }
}