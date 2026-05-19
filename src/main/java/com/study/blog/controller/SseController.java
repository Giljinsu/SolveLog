package com.study.blog.controller;

import com.study.blog.service.CustomUserDetails;
import com.study.blog.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class SseController {
    private final SseService sseService;

    @GetMapping("/api/sse/connect")
    public ResponseEntity<SseEmitter> connect(
        @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SseEmitter connect = sseService.connect(userDetails.getUserId(), lastEventId);
        return ResponseEntity.ok(connect);
    }
}
