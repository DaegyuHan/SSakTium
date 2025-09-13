package com.sparta.ssaktium.domain.coupon.batch;

import com.sparta.ssaktium.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponBatchService {

    private final ProgressStore progressStore;     // 1단계에서 만든 진행률 저장소
    private final CouponService couponService; // 네가 올린 기존 서비스 (createCoupons() 300개 생성)
    private final CouponBatchRunner runner;


    /** 배치 시작: jobId 생성 + 진행률 초기화 + 비동기 실행 */
    public String startJob(int total) {
        // total이 300으로 안 나누어 떨어져도 동작:
        // 마지막 루프에서 processed는 min(남은 수, 300) 만큼만 증가시켜 퍼센트는 정확히 맞춤
        String jobId = UUID.randomUUID().toString();
        progressStore.init(jobId, total, Duration.ofHours(12)); // TTL은 필요에 맞게
        runner.runJobAsync(jobId, total); // 비동기 실행
        return jobId;
    }

    /** 진행 상태 조회 (컨트롤러에서 그대로 반환 예정) */
    public BatchProgress getProgress(String jobId) {
        return progressStore.get(jobId);
    }

    /** 취소 */
    public void cancel(String jobId) {
        progressStore.setStatus(jobId, "CANCELED");
    }
}
