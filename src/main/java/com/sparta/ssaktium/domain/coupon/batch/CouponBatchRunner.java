package com.sparta.ssaktium.domain.coupon.batch;

import com.sparta.ssaktium.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponBatchRunner { // 새 클래스
    private final ProgressStore progressStore;
    private final CouponService couponService;

    private static final int CHUNK = 300; // 기존 로직 존중: 호출 1회당 300개 생성

    /** 실제 배치 실행 (@Async) — 기존 createCoupons()를 반복 호출 */
    @Async("couponBatchExecutor")
    public void runJobAsync(String jobId, int total) {
        log.info("⚡ [Runner] runJobAsync() 실행 thread={}", Thread.currentThread().getName());
        try {
            int processed = progressStore.get(jobId).processed(); // 0에서 시작
            while (processed < total) {
                // 1) 기존 로직 그대로 호출: 호출 1번 = 300개 생성
                couponService.createCoupons(); // 반환값(String)은 필요시 로그로만 사용

                // 2) 진행률 갱신 (남은 수만큼만 가산해서 퍼센트 정확히)
                int remaining = total - processed;
                int inc = Math.min(CHUNK, remaining);
                progressStore.addProcessed(jobId, inc);
                progressStore.incStep(jobId);

                processed += inc;

                if (processed % (CHUNK * 10) == 0 || processed >= total) {
                    log.info("job {} progress: {}/{} ({}%)",
                            jobId, processed, total,
                            Math.min(100, (int)Math.round(processed * 100.0 / total)));
                }
            }
            progressStore.setStatus(jobId, "DONE");
        } catch (Exception e) {
            log.error("job {} error", jobId, e);
            progressStore.setStatus(jobId, "ERROR");
        }
    }
}
