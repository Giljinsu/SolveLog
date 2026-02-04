package com.study.blog.service;

import com.study.blog.FileUpload;
import com.study.blog.S3Uploader;
import com.study.blog.dto.file.FileRequestDto;
import com.study.blog.dto.file.FileResponseDto;
import com.study.blog.entity.File;
import com.study.blog.entity.Users;
import com.study.blog.entity.enums.FileType;
import com.study.blog.exception.NotExistUserException;
import com.study.blog.repository.FileRepository;
import com.study.blog.repository.UsersRepository;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final UsersRepository usersRepository;
    private final FileUpload fileUpload;
    private final S3Uploader s3Uploader;

    public FileResponseDto findUserImgByUsername(String username) {
        Optional<File> optionalFile = fileRepository.findUserImgByUsername(username);

        return optionalFile
            .map(file -> new FileResponseDto(file.getId(), file.getOriginalFileName(),
                file.getPath()))
            .orElse(null);

    }

    //파일 업로드
    @Transactional
    public FileResponseDto fileUpload(FileRequestDto requestDto) {
        MultipartFile file = requestDto.getFile();
        Long postId = requestDto.getPostId();
        String username = requestDto.getUsername();
        Boolean isThumbnail = requestDto.getIsThumbnail();

        if (!StringUtils.hasText(file.getOriginalFilename())) {
            throw new RuntimeException("this file has wrong name");
        }

//        FileUpload fileUpload = new FileUpload();

        String savePath = fileUpload.fileUpload2(file, requestDto.getUsername(), postId);

        java.io.File physicalFile = new java.io.File(savePath);
        try {
            String extension = fileUpload.getExtension(file.getOriginalFilename());
            FileType fileType = FileType.getFileType(extension).orElseThrow();

            File newFile = File.createFile(
                savePath,
                username,
                postId,
                fileType,
                file.getOriginalFilename(),
                file.getSize(),
                isThumbnail,
                false
            );
            fileRepository.save(newFile);

            return new FileResponseDto(
                newFile.getId(),
                newFile.getPostId(),
                newFile.getOriginalFileName(),
                "/api/inlineFile/"+newFile.getId()
            );
        } catch (Exception e) {
            if (physicalFile.exists()) { // 엔티티에서 저장 문제가 생기면 해당 파일 삭제
                boolean deleted = physicalFile.delete();
            } else {
//                fileUpload.deleteFileFromS3(savePath);
                s3Uploader.delete(savePath);
            }
            throw e;
        }
    }

    //파일 다운
    public ResponseEntity<Resource> fileDownload(Long fileId) {
       return fileDownloadOrInline(fileId, "attachment");
    }

    //파일 미리보기
    public ResponseEntity<Resource> fileInline(Long fileId) {
        return fileDownloadOrInline(fileId, "inline");
    }

    // temp 폴더에서 파일 이동
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void moveFileToPostId(Long postId) {
//        FileUpload fileUpload = new FileUpload();
        List<File> fileList = fileRepository.findFileByPostId(postId).orElseThrow();
        if (fileList.isEmpty()) return;
        File firstFile = fileList.getFirst();

        String path = firstFile.getPath();
        String username = firstFile.getUsername();

        try {
            Path filePath = Paths.get(path); // ex: upload/2025/07/jinsu/temp/file123.png
            Path tempDir = filePath.getParent().getParent().resolve("temp");       // → upload/2025/07/jinsu/temp

            // temp 폴더가 없는경우 넝어가기
            if (!Files.isDirectory(tempDir)) {
                return;
            }

            Path rootDir = Paths.get("upload");
            String year = String.valueOf(LocalDate.now().getYear());
            String month = String.format("%02d",LocalDate.now().getMonthValue());
            String postDir = String.valueOf(postId);

            Path targetDir = Paths.get(rootDir.toString(), year, month, username, postDir);

//            fileUpload.moveFileToTargetDir(tempDir, targetDir);
            fileUpload.moveFileToS3TargetDir(tempDir, targetDir);

            for (File file : fileList) {
                if (file.getIsUserImg()) continue; // 유저 이미지 제외
                Path originalFileName = Paths.get(file.getPath()).getFileName();

                file.updatePath(targetDir.resolve(originalFileName).toString());
            }

        } catch (IOException e) {
            throw new RuntimeException("failed move file to postId");
        }

    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFilePostId(Long postId, String username) {

        fileRepository.bulkUpdatePostIdWherePostIdIsNull(postId, username);
    }

    //파일 삭제
    @Transactional
    public void deleteFile(Long fileId) {
        File fileEntity = fileRepository.findById(fileId)
            .orElseThrow(() -> new IllegalStateException("not found file"));

        java.io.File findFile = new java.io.File(fileEntity.getPath());

        if (!findFile.exists()) {
//            throw new RuntimeException("file doesn't exist");
//            fileUpload.deleteFileFromS3(fileEntity.getPath());
            s3Uploader.delete(fileEntity.getPath());
        } else {
            findFile.delete();
        }


        fileRepository.deleteById(fileId);
    }

    //파일 삭제 (임시파일)
    @Transactional
    public void deleteTempFile(String username) {
        // 해당 유저의 임시파일(postId가 존재하지 않은 File) 삭제
//        List<File> files = fileRepository.findByUsernameAndPostIdIsNull(username).orElseThrow();
        List<File> files = fileRepository.findUsingFileByUsername(username).orElseThrow();


        for (File file : files) {
            java.io.File physicalFile = new java.io.File(file.getPath());
            if (physicalFile.exists() && !file.getIsUserImg()) {
                physicalFile.delete();
            }

            fileRepository.deleteById(file.getId());
        }

    }

    //파일 삭제 (게시물 Id)
    @Transactional
    public void deleteFilesByPostId(Long postId) {
        List<File> files = fileRepository.findFileByPostId(postId)
            .orElseThrow(() -> new IllegalStateException("not found files"));

        for (File fileEntity : files) {
//            java.io.File findFile = new java.io.File(fileEntity.getPath());
//
//            if (!findFile.exists()) {
//                throw new RuntimeException("file not found in our server");
//            }
//
//            findFile.delete();
//            fileUpload.deleteFileFromS3(fileEntity.getPath());
            s3Uploader.delete(fileEntity.getPath());
        }

        fileRepository.deleteByPostId(postId);
    }

    @Transactional
    public FileResponseDto uploadUserImg(FileRequestDto requestDto) {
//        FileUpload fileUpload = new FileUpload();

        MultipartFile file = requestDto.getFile();
        String username = requestDto.getUsername();
        Long postId = requestDto.getPostId();

        if (username.isEmpty()) throw new IllegalStateException("username is empty");

        if (!StringUtils.hasText(file.getOriginalFilename())) {
            throw new RuntimeException("this file has wrong name");
        }

        String savePath = fileUpload.uploadUserImg(file, username);

        java.io.File physicalFile = new java.io.File(savePath);
        try {
            String extension = fileUpload.getExtension(file.getOriginalFilename());
            FileType fileType = FileType.getFileType(extension).orElseThrow();

            File newFile = File.createFile(
                savePath,
                username,
                postId,
                fileType,
                file.getOriginalFilename(),
                file.getSize(),
                false,
                true
            );
            fileRepository.save(newFile);

            Users findUser = usersRepository.findUsersByUsername(username)
                .orElseThrow(NotExistUserException::new);

            findUser.uploadUserImg(newFile.getId());

            return new FileResponseDto(
                newFile.getId(),
                newFile.getPostId(),
                newFile.getOriginalFileName(),
                "/api/inlineFile/"+newFile.getId()
            );
        } catch (Exception e) {
            if (physicalFile.exists()) { // 엔티티에서 저장 문제가 생기면 해당 파일 삭제
                boolean deleted = physicalFile.delete();
            } else {
//                fileUpload.deleteFileFromS3(savePath);
                s3Uploader.delete(savePath);
            }
            throw e;
        }

    }


    private ResponseEntity<Resource> fileDownloadOrInline(Long fileId, String type) {
        File file = fileRepository.findById(fileId).orElseThrow(()-> new IllegalArgumentException("파일이 존재하지 않습니다."));

        String encodeName;
        try {
            // 브라우저에서 파일이름 깨짐 방지
            encodeName = URLEncoder.encode(file.getOriginalFileName(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            encodeName = file.getOriginalFileName();
        }

        java.io.File findFile = new java.io.File(file.getPath());


        if (!findFile.exists()) {
//            throw new RuntimeException("파일이 서버에 존재 하지 않습니다.");
            // temp 파일 존재 하지 않다면
            String fullUrl = s3Uploader.getFullUrl(file.getPath());
            if ("attachment".equalsIgnoreCase(type)) {
                // S3에게 강제로 다운로드 헤더를 붙이도록 요청
//                S3의 response-content-disposition 파라미터는
//                S3가 반환할 때 Content-Disposition 헤더로 변환해서 내려줍니다.
//                그래서 브라우저가 그걸 보고 다운로드를 수행합니다.
                fullUrl += "?response-content-disposition=attachment%3B%20filename%3D\"" + encodeName + "\"";
            }
//            else {
//                fullUrl += "?response-content-disposition=inline%3B%20filename%3D\"" + encodeName + "\"";
//            }
            fullUrl = fullUrl.replace(" ", "+");



            return ResponseEntity.status(HttpStatus.FOUND)  // 302 Redirect
                .location(URI.create(fullUrl))
                .build();
        }

        FileSystemResource fileSystemResource = new FileSystemResource(findFile);


        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(file.getType().getMimeType())) // MIME 타입을 명시적으로 알려줘서, 브라우저가 어떤 방식으로 처리할지 결정
            .header(HttpHeaders.CONTENT_DISPOSITION, type + "; filename=\"" + encodeName + "\"") //
            .body(fileSystemResource);
        /*
        .contentType : MIME 타입을 명시적으로 알려줘서, 브라우저가 어떤 방식으로 처리할지 결정
        .header : 파일로 다운로드 시킬지 여부를 지정
                  "attachment" 가 들어가면 다운로드
                  "inline" 이면 브라우저에서 미리보기 시도
        .body : 실제 파일 데이터를 포함
         */
    }

    public ResponseEntity<Resource> getUserImgInline(String username) {
        File userImg = fileRepository.findUserImgByUsername(username).orElseThrow(() -> new IllegalArgumentException("파일이 존재하지 않습니다."));
//        File file = fileRepository.findById(fileId).orElseThrow(()-> new IllegalArgumentException("파일이 존재하지 않습니다."));

//        String encodeName;
//        try {
//            // 브라우저에서 파일이름 깨짐 방지
//            encodeName = URLEncoder.encode(userImg.getOriginalFileName(), StandardCharsets.UTF_8);
//        } catch (Exception e) {
//            encodeName = userImg.getOriginalFileName();
//        }

        String fullUrl = s3Uploader.getFullUrl(userImg.getPath());
//        fullUrl += "?response-content-disposition=inline%3B%20filename%3D\"" + encodeName + "\"";

        return ResponseEntity.status(HttpStatus.FOUND)  // 302 Redirect
            .location(URI.create(fullUrl))
            .build();
    }


}
