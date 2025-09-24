package com.study.blog.controller;

import com.study.blog.dto.file.FileRequestDto;
import com.study.blog.dto.file.FileResponseDto;
import com.study.blog.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FileController {
    private  final FileService fileService;

    // 생성
    @PostMapping("/api/uploadFile")
    public ResponseEntity<FileResponseDto> uploadFile(@ModelAttribute FileRequestDto requestDto) {
        FileResponseDto fileResponseDto = fileService.fileUpload(requestDto);
        return ResponseEntity.ok(fileResponseDto);
    }

    // 유저 이미지 업로드
    @PostMapping("/api/uploadUserImg")
    public ResponseEntity<FileResponseDto> uploadUserImg(@ModelAttribute FileRequestDto requestDto) {
        FileResponseDto fileResponseDto = fileService.uploadUserImg(requestDto);
        return ResponseEntity.ok(fileResponseDto);
    }

    // 유저 이미지 조회
    @GetMapping("/api/getUserImg/{username}")
    public ResponseEntity<FileResponseDto> getUserImg(@PathVariable String username) {
        return ResponseEntity.ok(fileService.findUserImgByUsername(username));
    }

    // 임시저장 파일삭제
    @PostMapping("/api/deleteTempFiles/{username}")
    public ResponseEntity<Void> deleteTempFiles(@PathVariable String username) {
        fileService.deleteTempFile(username);
        return ResponseEntity.noContent().build();
    }

    //파일 다운로드
    @GetMapping("api/downloadFile/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        return fileService.fileDownload(fileId);
    }

    //파일 미릭보기
    @GetMapping("api/inlineFile/{fileId}")
    public ResponseEntity<Resource> inlineFile(@PathVariable Long fileId) {
        return fileService.fileInline(fileId);
    }

    //파일 삭제
    @PostMapping("api/deleteFile/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    //유저이미지 미리보기
    @GetMapping("api/inlineUserImg/{username}")
    public ResponseEntity<Resource> inlineFile(@PathVariable String username) {
        return fileService.getUserImgInline(username);
    }
}
