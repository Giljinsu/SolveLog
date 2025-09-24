package com.study.blog.service.login;


import jakarta.persistence.criteria.CriteriaBuilder.In;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
public class LoginAttemptService {
    private final int MAX_ATTEMPT = 5;

//    private final Map<String, Integer> attemptCache = new Hashtable<>();
//    private final Map<String, Integer> attemptCache = new HashMap<>();
    private final Map<String, FailedLoginRecord> attemptCache = new ConcurrentHashMap<>();

    /*
        Hashtable: 대부분 메서드 전체에 synchronized 키워드 존재 그렇기 때문에 Multi-Thread 환경에서 나쁘지 않을수도?
        그렇지만 동시에 작업하려 하여도 각 객체마다 Lock 을 가지기 때문에 동시에 여러작업을 해야할 때 병목현상 발생 가능

        HashMap : 이 클래스는 synchronized 가 존재하지 않아 Multi-Thread 환경에서 사용할 수 없다 그렇지만 성능이 제일
        좋다고 한다.

        ConcurrentHashMap: Hashtable 단점을 보완하면서 멀티쓰레드 환경에서 사용할 수 있도록 나온 클래스 (JDK 1.5 업데이트)

     */

    public void loginFailed(String key) {
        FailedLoginRecord record = attemptCache.get(key);

        int attempt = 0;
        if (record != null) {
            attempt = record.getCount();
        }

        attemptCache.put(key, new FailedLoginRecord(attempt+1, System.currentTimeMillis()));
    }

    public void loginSucceeded(String key) {
        attemptCache.remove(key);
    }

    public boolean isBlocked(String key) {
        FailedLoginRecord record = attemptCache.get(key);

        if (record == null) {
            return false;
        }

        int attempts = record.getCount();
        long lastFailedTime = record.getLastFailedTime();

        return attempts >= MAX_ATTEMPT
            && System.currentTimeMillis() - lastFailedTime <= 10 * 60 * 1000;
    }

    @Data
    @AllArgsConstructor
    private static class FailedLoginRecord {
        int count;
        long lastFailedTime;

        public FailedLoginRecord(int count) {
            this.count = count;
        }
    }

}
