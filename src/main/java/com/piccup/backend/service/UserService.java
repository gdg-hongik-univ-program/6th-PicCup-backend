package com.piccup.backend.service;

import com.piccup.backend.dto.UserRequest;
import com.piccup.backend.entity.User;
import com.piccup.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public User signup(UserRequest.Signup request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // BCrypt를 사용해 비밀번호 단방향 암호화
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 정적 팩토리 메서드 활용
        User user = User.createUser(request.getEmail(), hashedPassword, request.getNickname(), null);
        return userRepository.save(user);
    }

    public User login(UserRequest.Login request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 입력받은 평문(request.getPassword())과 DB의 해시(user.getPasswordHash()) 일치 여부 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }

    @Transactional
    public void resetPassword(UserRequest.PasswordReset request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 새 비밀번호도 암호화해서 저장
        String hashedNewPassword = passwordEncoder.encode(request.getNewPassword());
    }
}