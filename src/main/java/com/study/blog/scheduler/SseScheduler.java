package com.study.blog.scheduler;

import com.study.blog.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SseScheduler {
    private final SseService sseService;

    @Scheduled(fixedRate = 30000)
    public void sendPing() {
        sseService.sendPing();
    }
}
