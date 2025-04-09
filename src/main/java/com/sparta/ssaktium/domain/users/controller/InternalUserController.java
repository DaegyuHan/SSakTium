package com.sparta.ssaktium.domain.users.controller;

import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.sparta.ssaktium.domain.common.dto.AuthUser;
import com.sparta.ssaktium.domain.users.dto.response.UserInfoResponseDto;
import com.sparta.ssaktium.domain.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/internal/users")
public class InternalUserController {

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponseDto> getMyInfo(@RequestHeader("X-User-Id") Long userId) {
        log.info("FeignClient 작동 "+ userId);
        return ResponseEntity.ok(new UserInfoResponseDto(userId));
    }
}
