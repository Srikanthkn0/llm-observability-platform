package com.llmobs.service;

import com.llmobs.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final StringRedisTemplate redis;

    @Value("${llmobs.rate-limit.traces-per-minute}")
    private long tracesPerMinute;

    public void checkTraceIngest(UUID projectId) {
        String key = "rl:traces:" + projectId + ":" + (System.currentTimeMillis() / 60_000);
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, Duration.ofMinutes(2));
        }
        if (count != null && count > tracesPerMinute) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Rate limit exceeded: max " + tracesPerMinute + " traces/minute");
        }
    }
}
