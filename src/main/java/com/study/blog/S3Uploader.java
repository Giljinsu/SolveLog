package com.study.blog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Component
//@Profile("prod")
public class S3Uploader {
    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final String bucket;

    public S3Uploader(
        @Value("${cloud.aws.s3.bucket}") String bucket,
        @Value("${cloud.aws.region.static}") String region,
        @Value("${cloud.aws.credentials.access-key:}") String accessKey,
        @Value("${cloud.aws.credentials.secret-key:}") String secretKey
    ) {
        this.bucket = bucket;

        if (accessKey.isEmpty()) { // ec2 IAM ROLE 사용떄
            this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .build();
            this.presigner = S3Presigner.builder()
                .region(Region.of(region))
                .build();
        } else {
            this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

            this.presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
        }
    }

    public String upload(MultipartFile file, String path) {
        // upload/2025/05/a/101/temp.png
        try {
            String key = path.replace(File.separator, "/");
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucket) // 업로드할 대상 버킷 이름
                    .key(key) // 버킷 안에서 파일이 저장될 "경로/파일명"
//                    .contentType(file.getContentType()+"; charset=utf-8") // 파일 MIME 타입 (ex: image/png, text/plain)
                    .contentType(file.getContentType()) // 파일 MIME 타입 (ex: image/png, text/plain)
//                    .acl(ObjectCannedACL.PUBLIC_READ) // 접근 권한 (PUBLIC_READ → URL로 공개 접근 가능)
                    .build(),
                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucket,
                s3Client.serviceClientConfiguration().region().id(),
                key);
        } catch (S3Exception | IOException e) {
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
        }
    }

    public String uploadFromFile(File file, String path) {
        try {
            String key = path.replace(File.separator, "/");
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(Files.probeContentType(file.toPath())) // 자동 MIME 추론
//                    .acl(ObjectCannedACL.PUBLIC_READ) // 공개 접근 가능
                    .build(),
                software.amazon.awssdk.core.sync.RequestBody.fromFile(file.toPath()) // MultipartFile 대신 File 직접 사용
            );

            return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucket,
                s3Client.serviceClientConfiguration().region().id(),
                key);
        } catch (IOException | S3Exception e) {
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
        }
    }



    public void delete(String path) {
        // upload/2025/05/a/101/temp.png
        String key = path.replace(File.separator, "/");
        try {
            s3Client.deleteObject(b-> b.bucket(bucket).key(key));
        } catch (Exception e) {
            throw new RuntimeException("S3 삭제 실패" + e.getMessage(), e);
        }
    }

    /* Create a pre-signed URL to download an object in a subsequent GET request. */
    public String createPresignedGetUrl(String keyName) {

        GetObjectRequest objectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(keyName)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))  // The URL will expire in 10 minutes.
            .getObjectRequest(objectRequest)
            .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toExternalForm();
    }


    public String getFullUrl(String path) {
//        String key = path.replace(File.separator, "/");
//        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
        return createPresignedGetUrl(path);
    }

}
