package com.sparta.ssaktium.domain.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisUserService {

    private final RedisTemplate<String, String> redisTemplate;

    // Redis 에 Refresh Token 저장
    public void saveRefreshToken(String userId, String refreshToken, long ttlInMillis) {
        String key = "refresh_Token : " + userId;
        redisTemplate.opsForValue().set(key, refreshToken, ttlInMillis, TimeUnit.MILLISECONDS);
    }

    // Refresh Token 조회
    public String getRefreshToken(String userId) {
        String key = "refresh_Token : " + userId;
        return redisTemplate.opsForValue().get(key);
    }

    // Refresh Token 삭제
    public void deleteRefreshToken(String userId) {
        String key = "refresh_Token : " + userId;
        redisTemplate.delete(key);
    }
}
