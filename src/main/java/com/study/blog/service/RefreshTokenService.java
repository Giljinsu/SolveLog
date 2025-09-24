package com.study.blog.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final StringRedisTemplate stringRedisTemplate;
//    private Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();
    private String userKey(String un) { return "rt:user:"+un; }

    public void saveToken(String username, String refreshToken) {
        if (username.isEmpty() || refreshToken.isEmpty()) return;

        stringRedisTemplate.opsForValue().set(
            userKey(username),
            refreshToken,
            Duration.ofDays(14)
        );


//        refreshTokenStore.put(username, refreshToken);
    }

    public void deleteToken(String username) {
        if (username.isEmpty()) return;

//        stringRedisTemplate.opsForSet().remove(userKey(username));
        stringRedisTemplate.delete(userKey(username));
//        refreshTokenStore.remove(username);
    }

    public boolean validateToken(String username, String refreshToken) {
        if(!stringRedisTemplate.hasKey(userKey(username))) return false;

        String storedToken = stringRedisTemplate.opsForValue().get(userKey(username));

        if (storedToken == null ) return false;

        return storedToken.equals(refreshToken);
    }
}
