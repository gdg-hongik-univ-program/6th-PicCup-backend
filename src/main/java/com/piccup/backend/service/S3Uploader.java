package com.piccup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

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

    // copy 성공해야 원본 delete (실패 시 원본 보존 → 데이터 안 날아감)
    private void copyAndDeleteOriginal(String sourceKey, String destKey) {
        s3Client.copyObject(CopyObjectRequest.builder()
                .sourceBucket(bucket).sourceKey(sourceKey)
                .destinationBucket(bucket).destinationKey(destKey)
                .build());
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket).key(sourceKey)
                .build());
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
}
