package com.study.blog;

import com.study.blog.entity.enums.FileType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Io;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class FileUpload {
    private final S3Uploader s3Uploader;

    //파일 업로드
    //교육용 사용 X
    public String fileUpload(MultipartFile file) throws IOException {
        String uploadRootPath = "upload";

        LocalDateTime now = LocalDateTime.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());

        String fullPath = uploadRootPath + File.separator + year + File.separator + month;
        // "/" 대신 File.separator 이걸로 리눅스 윈도우 둘다 대앙

        File uploadDir = new File(fullPath);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (!created) {
                throw new RuntimeException("폴더 생성 실패" + fullPath);
            }
        }

        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + "_" + file.getOriginalFilename();

        File saveFile = new File(uploadDir, savedFileName);
        file.transferTo(saveFile);

        return fullPath + File.separator + savedFileName;
    }


    //파일 업로드
    public String fileUpload2(MultipartFile file, String username, Long postId) {
        // Paths Files 사용

        Path rootDir = Paths.get("upload");
        String year = String.valueOf(LocalDate.now().getYear());
        String month = String.format("%02d",LocalDate.now().getMonthValue());
        String postDir = postId != null ? String.valueOf(postId) : "temp";

        Path fullPath = Paths.get(rootDir.toString(), year, month, username, postDir);

        if (postId == null) {
            // temp 일 경우 내부에 이미지 저장
            return createFileAndGetAbsolutePath(file, fullPath);
        } else {
            // 아닐경우 s3에 업로드
            return uploadS3AndGetAbsolutePath(file, fullPath);
        }

    }

    private String createFileAndGetAbsolutePath(MultipartFile file, Path fullPath) {
        if(!Files.exists(fullPath)) {
            try {
                Files.createDirectories(fullPath);
            } catch (IOException e) {
                throw new RuntimeException("폴더 생성 실패 : " + fullPath);
            }
        }
//        String uuid = UUID.randomUUID().toString();
//        String savedFileName = uuid + "_" + file.getOriginalFilename();
//
//        Path target = fullPath.resolve(savedFileName);
        Path target = addUuidOnPath(fullPath, file.getOriginalFilename());

        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new RuntimeException("파일 생성 실패 :" + target.toFile());
        }

        return target.toString();
    }

    private String uploadS3AndGetAbsolutePath(MultipartFile file, Path fullPath) {
        if (file.getOriginalFilename() == null) {
            throw new RuntimeException("파일이 없습니다");
        }


        Path target = addUuidOnPath(fullPath, file.getOriginalFilename());
//        String key = target.toString().replace(File.separator, "/");
        s3Uploader.upload(file, target.toString());
        return target.toString();
    }

    private Path addUuidOnPath(Path fullPath, String originalFilename) {
        String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8);
//            .replace("+", "%20");
        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + "_" + encodedFilename;

        return fullPath.resolve(savedFileName);
    }

    public String uploadUserImg(MultipartFile file, String username) {
        Path rootDir = Paths.get("upload","userImg",username);

        return uploadS3AndGetAbsolutePath(file, rootDir);
    }

    // 폴더 이동
    public void moveOneFileToTargetDir(Path targetDir, com.study.blog.entity.File file) throws IOException {
        if(!Files.exists(targetDir)) {
            try {
                Files.createDirectories(targetDir);
            } catch (IOException e) {
                throw new RuntimeException("폴더 생성 실패 : " + targetDir);
            }
        }

        Path originalFilePath = Paths.get(file.getPath());
        if (!Files.exists(originalFilePath)) {
            throw new IOException("원본 파일이 존재하지 않음: " + originalFilePath);
        }

        Path targetPath = targetDir.resolve(originalFilePath.getFileName());
        Files.move(originalFilePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

    }

    // 폴더 이동
    public void moveFileToTargetDir(Path originDir, Path targetDir) throws IOException {
        // s3 사용하지 않을때
        if (!Files.exists(originDir)) {
            throw new IOException("Temp directory does not exist");
        }

        if(!Files.exists(targetDir)) {
            try {
                Files.createDirectories(targetDir);
            } catch (IOException e) {
                throw new RuntimeException("폴더 생성 실패 : " + targetDir);
            }
        }

        // temp 디렉토리 내 모든 파일 이동
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(originDir)) {
            for (Path file : stream) {
                Path targetPath = targetDir.resolve(file.getFileName());
                Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    // s3로 이동
    public void moveFileToS3TargetDir(Path originDir, Path targetDir) throws IOException {
        if (!Files.exists(originDir)) {
            throw new IOException("Temp directory does not exist");
        }

        // temp 디렉토리 내 모든 파일 이동
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(originDir)) {
            for (Path file : stream) {
                try {
//                    String key = targetDir.resolve(file.getFileName()).toString().replace(File.separator, "/");
                    Path targetPath = targetDir.resolve(file.getFileName());
                    s3Uploader.uploadFromFile(file.toFile(), targetPath.toString());
                    Files.delete(file);
                } catch (Exception e) {
                    System.err.println("업로드 실패: " + file + "->" + e.getMessage());
                }
//                Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    public void deleteFileFromS3(String savePath) {
//        String key = savePath.replace(File.separator, "/");
        s3Uploader.delete(savePath);
    }


    public String getExtension(String originalFileName) {
//        System.out.println("originalFileName = " + originalFileName);
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.') + 1);
        return extension.toLowerCase();
    }
}
