package com.study.blog.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseRepository {
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 30; // 30분
    private final Map<String, SseEmitter> emitterHashMap = new ConcurrentHashMap<>();

    private String sseId(Long id) { return "user" + id.toString(); }

    public SseEmitter save(Long id) {
        emitterHashMap.put(sseId(id), new SseEmitter(DEFAULT_TIMEOUT));
        return emitterHashMap.get(sseId(id));
    }

    public void delete(Long id) {
        emitterHashMap.remove(sseId(id));
    }

    public void deleteBySseId(String sseId) {
        emitterHashMap.remove(sseId);
    }

    public SseEmitter getSseEmitter(Long id) {
        return emitterHashMap.get(sseId(id));
    }

    public Map<String, SseEmitter> findAll() {
        return emitterHashMap;
    }
}
