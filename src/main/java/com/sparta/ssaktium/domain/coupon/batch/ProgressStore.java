package com.sparta.ssaktium.domain.coupon.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProgressStore {

    private final StringRedisTemplate redis;

    private static String key(String jobId) {
        return "coupon:batch:" + jobId;
    }

    /** 작업 초기화 */
    public void init(String jobId, int total, Duration ttl) {
        String k = key(jobId);
        redis.opsForHash().put(k, "total", String.valueOf(total));  // 목표 총 건수
        redis.opsForHash().put(k, "processed", "0");    // 지금까지 처리한 수 (증분 저장)
        redis.opsForHash().put(k, "status", "RUNNING");
        redis.opsForHash().put(k, "step", "0"); // CHUNK 단위로 몇번 돌았는지 (디버깅/모니터링 목적 )
        if (ttl != null) {
            redis.expire(k, ttl);
        }
    }

    /** 진행 증가 (건 수) */
    public void addProcessed(String jobId, int delta) {
        redis.opsForHash().increment(key(jobId), "processed", delta);
    }

    /** 청크 1회 완료 시 step 증가 */
    public void incStep(String jobId) {
        redis.opsForHash().increment(key(jobId), "step", 1);
    }

    /** 상태 변경 */
    public void setStatus(String jobId, String status) {
        redis.opsForHash().put(key(jobId), "status", status);
    }

    /** 조회 */
    public BatchProgress get(String jobId) {
        var k = key(jobId);
        var vals = redis.opsForHash().multiGet(k, List.of("total", "processed", "status", "step"));
        if (vals == null || vals.get(0) == null) {
            return new BatchProgress(0, 0, "NOT_FOUND", 0);
        }
        int total = Integer.parseInt((String) vals.get(0));
        int processed = Integer.parseInt((String) vals.get(1));
        String status = (String) vals.get(2);
        int step = Integer.parseInt((String) vals.get(3));
        return new BatchProgress(total, processed, status, step);
    }
}
