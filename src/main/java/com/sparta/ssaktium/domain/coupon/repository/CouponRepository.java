package com.sparta.ssaktium.domain.coupon.repository;

import com.sparta.ssaktium.domain.coupon.entity.Coupon;
import com.sparta.ssaktium.domain.coupon.enums.CouponStatus;
import com.sparta.ssaktium.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Coupon findFirstByCouponStatus(CouponStatus couponStatus);

    // 미발급 쿠폰 개수 조회
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.couponStatus = :couponStatus")
    long countByCouponStatus(CouponStatus couponStatus);

    // 이미 발급 받은 유저인지 확인
    boolean existsByUserAndCouponStatus(User user, CouponStatus couponStatus);

    Coupon findTopByCouponStatus(CouponStatus couponStatus);
}
