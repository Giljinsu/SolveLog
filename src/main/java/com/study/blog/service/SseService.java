package com.study.blog.service;

import com.study.blog.dto.alarm.AlarmResponseDto;
import com.study.blog.entity.Users;
import com.study.blog.exception.NotExistUserException;
import com.study.blog.repository.SseRepository;
import com.study.blog.repository.UsersRepository;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class SseService {
    private final SseRepository sseRepository;
    private final UsersRepository usersRepository;

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
        } catch (IOException e) {
            sseRepository.delete(userId);
        }

        if (!lastEventId.isEmpty()) {

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
                .id(userId.toString())
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
