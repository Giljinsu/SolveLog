package com.study.blog;

import com.study.blog.entity.enums.FileType;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Io;
import org.springframework.web.multipart.MultipartFile;

public class FileUpload {
    //파일 업로드
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

        return createFileAndGetAbsolutePath(file, fullPath);
    }

    private static String createFileAndGetAbsolutePath(MultipartFile file, Path fullPath) {
        if(!Files.exists(fullPath)) {
            try {
                Files.createDirectories(fullPath);
            } catch (IOException e) {
                throw new RuntimeException("폴더 생성 실패 : " + fullPath);
            }
        }
        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + "_" + file.getOriginalFilename();

        Path target = fullPath.resolve(savedFileName);

        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new RuntimeException("파일 생성 실패 :" + target.toFile());
        }

        return target.toString();
    }

    public String uploadUserImg(MultipartFile file, String username) {
        Path rootDir = Paths.get("upload","userImg",username);

        return createFileAndGetAbsolutePath(file, rootDir);
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

    public String getExtension(String originalFileName) {
//        System.out.println("originalFileName = " + originalFileName);
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.') + 1);
        return extension.toLowerCase();
    }
}
