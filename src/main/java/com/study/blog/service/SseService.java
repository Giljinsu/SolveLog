package com.study.blog.service;

import com.study.blog.dto.alarm.AlarmResponseDto;
import com.study.blog.repository.SseRepository;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class SseService {
    private final SseRepository sseRepository;

    public SseEmitter connect(Long userId, String lastEventId) {
        SseEmitter emitter = sseRepository.save(userId);

        emitter.onCompletion(()->
            sseRepository.delete(userId)
        );
        emitter.onTimeout(()->
            sseRepository.delete(userId)
        );
        emitter.onError((e)->
            sseRepository.delete(userId)
        );

        try {
            // 처음 보내는 응답
            emitter.send(SseEmitter.event()
                .id(String.valueOf(System.currentTimeMillis()))
                .name("connect")
                .data("connected"));

            // Last-Event-Id 기반 누락 확장 가능
        } catch (IOException e) {
            sseRepository.delete(userId);
        }


        return emitter;
    }

    public void sendAlarm(Long userId, List<AlarmResponseDto> responseDto) {
        SseEmitter emitter = sseRepository.getSseEmitter(userId);

        if (emitter == null) {
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                .name("alarm")
                .data(responseDto));

        } catch (IOException e) {
            sseRepository.delete(userId);
        }
    }

    public void sendPing() {
        sseRepository.findAll().forEach((sseId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("ping")
                    .data("keep-alive"));
            } catch (IOException e) {
                sseRepository.deleteBySseId(sseId);
            }
        });
    }
}
