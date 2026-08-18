package com.piccup.backend.service;

import com.piccup.backend.dto.UserRequest;
import com.piccup.backend.dto.UserResponse;
import com.piccup.backend.entity.BestPick;
import com.piccup.backend.entity.Category;
import com.piccup.backend.entity.User;
import com.piccup.backend.exception.BusinessException;
import com.piccup.backend.exception.ErrorCode;
import com.piccup.backend.repository.BestPickRepository;
import com.piccup.backend.repository.CategoryRepository;
import com.piccup.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final BestPickRepository bestPickRepository;
    private final S3Uploader s3Uploader;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Transactional
    public User signup(UserRequest.Signup request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_DUPLICATE);
        }

        // BCrypt를 사용해 비밀번호 단방향 암호화
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 정적 팩토리 메서드 활용
        User user = User.createUser(request.getEmail(), hashedPassword, request.getNickname(), null);
        User savedUser = userRepository.save(user);

        // 미분류 카테고리 자동 시드 (isDefault=true) — 폴백 안전망
        // 미분류 카테고리 삭제 시 삭제할 것
        Category uncategorized = Category.createCategory(savedUser, "미분류", true);
        categoryRepository.save(uncategorized);

        return savedUser;
    }

    public User login(UserRequest.Login request) {
        // 이메일 존재 여부와 비밀번호 불일치 메시지 통일
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        return user;
    }

    @Transactional
    public void resetPassword(UserRequest.PasswordReset request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 업데이트
        String hashedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.updatePassword(hashedNewPassword);
    }

    // 내 정보 조회
    @Transactional(readOnly = true)
    public UserResponse.MyInfo getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String imageUrl = user.getProfileImageS3Key() != null ?
                s3Uploader.generatePresignedUrl(user.getProfileImageS3Key()) : null;

        return new UserResponse.MyInfo(user.getId(), user.getEmail(), user.getNickname(), imageUrl);
    }

    // 프로필 닉네임 수정
    @Transactional
    public UserResponse.UpdateNickname updateNickname(Long userId, UserRequest.UpdateNickname request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updateNickname(request.getNickname());

        return new UserResponse.UpdateNickname(user.getId(), user.getNickname());
    }

    // 프로필 사진 수정 (직접 업로드 or BestPic 복사)
    public UserResponse.ProfileImage updateProfileImage(Long userId, MultipartFile file, Long bestPickId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String oldKey = user.getProfileImageS3Key();
        String newKey;

        if (file != null && !file.isEmpty()) {
            // case 1: 폰에서 직접 사진 업로드
            newKey = s3Uploader.uploadProfile(file, userId);
        } else if (bestPickId != null) {
            // case 2: 기존 Best Pick에서 선택하여 복사
            BestPick pick = bestPickRepository.findById(bestPickId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.BEST_PICK_NOT_FOUND));

            // 소유권 검증
            if (!pick.getUser().getId().equals(userId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN_RESOURCE);
            }
            newKey = s3Uploader.copyToProfile(pick.getS3Key(), userId);
        } else {
            throw new BusinessException(ErrorCode.NO_IMAGE_PROVIDED);
        }

        // 새 이미지 키 저장 및 기존 이미지가 있었다면 삭제
        user.updateProfileImage(newKey);
        userRepository.save(user); // 트랜잭션 별도 적용 (고아 객체 발생 방지)

        if (oldKey != null) {
            try { s3Uploader.delete(oldKey); } catch (Exception e) {log.warn("기존 이미지 삭제 실패",e);}
        }

        return new UserResponse.ProfileImage(s3Uploader.generatePresignedUrl(newKey));
    }
}