package com.piccup.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    // yml의 버킷 이름을 읽어옴
    @org.springframework.beans.factory.annotation.Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file, Long userId) {
        // 1. S3에 저장할 key(경로+파일명) 생성
        String key = createKey(userId);

        // 2. S3 업로드 요청 만들기
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        // 3. 실제 업로드
        try {
            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }

        // 4. DB엔 이 key를 저장 (URL 아님)
        return key;
    }

    // key 규칙: best-picks/{userId}/{yyyyMM}/{uuid}.jpg
    private String createKey(Long userId) {
        String yearMonth = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
        String uuid = UUID.randomUUID().toString();
        return "best-picks/" + userId + "/" + yearMonth + "/" + uuid + ".jpg";
    }

    // presigned GET URL 생성 (조회용, 10분 유효)
    public String generatePresignedUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest)
                .url()
                .toString();
    }

    public void delete(String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder().bucket(bucket).key(key).build()
        );
    }

    // 소프트삭제: 원본 → best-picks/trash/... 로 이동. 새 key 반환(DB에 저장해야 함)
    public String moveToTrash(String originalKey) {
        String trashKey = toTrashKey(originalKey);
        copyAndDeleteOriginal(originalKey, trashKey);
        return trashKey;
    }

    // 복구: trash key → 원래 위치로. 새 key(=원래 key) 반환
    public String restoreFromTrash(String trashKey) {
        String originalKey = toOriginalKey(trashKey);
        copyAndDeleteOriginal(trashKey, originalKey);
        return originalKey;
    }

    private void copyAndDeleteOriginal(String sourceKey, String destKey) {
        copyObject(sourceKey, destKey);
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket).key(sourceKey).build());
    }

    private String toTrashKey(String key) {
        if (key.startsWith("best-picks/trash/")) {
            throw new IllegalStateException("이미 trash 상태인 key: " + key);
        }
        return key.replaceFirst("^best-picks/", "best-picks/trash/");
    }

    private String toOriginalKey(String trashKey) {
        if (!trashKey.startsWith("best-picks/trash/")) {
            throw new IllegalStateException("trash 상태가 아닌 key: " + trashKey);
        }
        return trashKey.replaceFirst("^best-picks/trash/", "best-picks/");
    }

    // 복사만 한다. 원본 삭제는 DB 갱신 후 호출자가 따로 처리
    public String copyToTrash(String originalKey) {
        String trashKey = toTrashKey(originalKey);
        copyObject(originalKey, trashKey);
        return trashKey;
    }

    public String copyToOriginal(String trashKey) {
        String originalKey = toOriginalKey(trashKey);
        copyObject(trashKey, originalKey);
        return originalKey;
    }

    // Lifecycle로 이미 만료된 객체인지 확인 (복구 전 방어)
    public boolean exists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket).key(key).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    // 삭제 실패가 흐름을 끊으면 안 되는 자리용
    public void deleteQuietly(String key) {
        try {
            delete(key);
        } catch (Exception e) {
            log.warn("S3 객체 삭제 실패 (미삭제 객체 잔존): {}", key, e);
        }
    }

    private void copyObject(String sourceKey, String destKey) {
        s3Client.copyObject(CopyObjectRequest.builder()
                .sourceBucket(bucket).sourceKey(sourceKey)
                .destinationBucket(bucket).destinationKey(destKey)
                .build());
    }

    // 이하 프로필 사진 업로드
    // 1. 프로필 전용 사진 업로드
    public String uploadProfile(MultipartFile file, Long userId) {
        String key = "profiles/" + userId + "/" + UUID.randomUUID() + ".jpg";
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();
        try {
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("프로필 사진 업로드 실패", e);
        }
        return key;
    }

    // 2. Best Pic 사진을 프로필용으로 S3 내부에서 복사
    public String copyToProfile(String sourceKey, Long userId) {
        String destKey = "profiles/" + userId + "/" + UUID.randomUUID() + ".jpg";

        s3Client.copyObject(CopyObjectRequest.builder()
                .sourceBucket(bucket).sourceKey(sourceKey)
                .destinationBucket(bucket).destinationKey(destKey)
                .build());
        return destKey;
    }
}
