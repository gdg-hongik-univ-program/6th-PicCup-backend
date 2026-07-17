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

        User user = new User();
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        // 암호화 적용
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        user.setPasswordHash(hashedPassword);

        return userRepository.save(user);
    }

    public User login(UserRequest.Login request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 비밀번호 비교: 평문 비밀번호(request.getPassword())와 DB의 해시값(user.getPasswordHash()) 비교
        // 해시는 단방향이므로 원본을 알 수 없어, BCrypt 내부 로직이 두 개가 일치하는지만 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }

    @Transactional
    public void resetPassword(UserRequest.PasswordReset request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        String hashedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(hashedNewPassword);
    }
}