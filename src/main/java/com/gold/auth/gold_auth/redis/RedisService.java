package com.gold.auth.gold_auth.redis;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.refresh-token-validate-in-seconds}") // Refresh Token 만료 시간
    private long refreshTokenExpiration;

    // Refresh Token을 Redis에 저장
    public void saveRefreshToken(String userId, String refreshToken) {
        redisTemplate.opsForValue().set(
            getRefreshTokenKey(userId),  // Redis에 저장할 키
            refreshToken,                  // 저장할 Refresh Token 값
            refreshTokenExpiration,        // 만료 시간
            TimeUnit.SECONDS          // 시간 단위
        );
    }

    // Refresh Token을 Redis에서 조회
    public String getRefreshToken(String userId) {
        return (String) redisTemplate.opsForValue().get(getRefreshTokenKey(userId));
    }

    // Refresh Token을 Redis에서 삭제
    public void deleteRefreshToken(String userId) {
        redisTemplate.delete(getRefreshTokenKey(userId));
    }

    // Redis에 저장할 Refresh Token 키 생성
    private String getRefreshTokenKey(String userId) {
        return "refreshToken:" + userId;
    }
}
