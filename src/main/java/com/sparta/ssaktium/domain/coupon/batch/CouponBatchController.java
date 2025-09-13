package com.sparta.ssaktium.domain.coupon.batch;

import com.sparta.ssaktium.config.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CouponBatchController {

    private final CouponBatchService couponBatchService;

    /** 진행바 페이지 (원하는 위치로 라우팅) */
    @GetMapping("/ssaktium/coupon-batch")
    public ModelAndView page() {
        // templates/coupon-batch.html 로 라우팅 (다음 단계에서 템플릿 제공)
        return new ModelAndView("coupon-batch");
    }

    /** 배치 시작: total(기본 10000)을 받아서 jobId 반환 */
    @Secured("ROLE_ADMIN")
    @PostMapping("/v2/coupon-batch/start")
    @ResponseBody
    public ResponseEntity<CommonResponse<String>> start(@RequestParam(defaultValue = "10000") int total) {
        log.info("🚀 [Controller] start() 호출 thread={}", Thread.currentThread().getName());
        String jobId = couponBatchService.startJob(total); // 비동기 시작
        return ResponseEntity.ok(CommonResponse.success(jobId));
    }

    /** 진행 조회: 진행바 폴링용 */
    @GetMapping("/v2/coupon-batch/{jobId}/progress")
    @ResponseBody
    public ResponseEntity<CommonResponse<BatchProgress>> progress(@PathVariable String jobId) {
        log.info("📊 [Controller] progress() 호출 thread={}", Thread.currentThread().getName());

        BatchProgress progress = couponBatchService.getProgress(jobId);
        return ResponseEntity.ok(CommonResponse.success(progress));
    }

    /** 취소 */
    @Secured("ROLE_ADMIN")
    @PostMapping("/v2/coupon-batch/{jobId}/cancel")
    @ResponseBody
    public ResponseEntity<CommonResponse<String>> cancel(@PathVariable String jobId) {
        couponBatchService.cancel(jobId);
        return ResponseEntity.ok(CommonResponse.success("OK"));
    }
}
