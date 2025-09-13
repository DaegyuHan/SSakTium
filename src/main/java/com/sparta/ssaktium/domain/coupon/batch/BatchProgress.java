package com.sparta.ssaktium.domain.coupon.batch;

public record BatchProgress(int total, int processed, String status, int step) {
    public int percent() {
        if (total <= 0) return 0;
        return Math.min(100, (int)Math.round(processed * 100.0 / total));
    }
}
