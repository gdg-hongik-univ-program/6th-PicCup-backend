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
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private static final String LOGIN_USER = "LOGIN_USER_ID";

    @PostMapping("/signup")
    public ResponseEntity<UserResponse.Signup> signup(@Valid @RequestBody UserRequest.Signup request) {
        User savedUser = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserResponse.Signup(savedUser.getId(), savedUser.getEmail(), savedUser.getNickname()));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse.Login> login(@Valid @RequestBody UserRequest.Login request, HttpServletRequest httpRequest) {
        User loginUser = userService.login(request);

        // 기존 세션 있으면 무효화
        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        // 로그인 성공 시 새로운 세션 발급 (DB에 저장)
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
    public ResponseEntity<UserResponse.Success> resetPassword(@Valid @RequestBody UserRequest.PasswordReset request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(new UserResponse.Success(true));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse.MyInfo> getMyInfo(
            @SessionAttribute(name = LOGIN_USER) Long userId) {
        return ResponseEntity.ok(userService.getMyInfo(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse.UpdateNickname> updateNickname(
            @SessionAttribute(name = LOGIN_USER) Long userId,
            @Valid @RequestBody UserRequest.UpdateNickname request) {
        return ResponseEntity.ok(userService.updateNickname(userId, request));
    }

    @PutMapping("/me/profile-image")
    public ResponseEntity<UserResponse.ProfileImage> updateProfileImage(
            @SessionAttribute(name = LOGIN_USER) Long userId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "bestPickId", required = false) Long bestPickId) {

        return ResponseEntity.ok(userService.updateProfileImage(userId, file, bestPickId));
    }
}