package com.sparta.ssaktium.domain.users.controller;

import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.sparta.ssaktium.domain.common.dto.AuthUser;
import com.sparta.ssaktium.domain.users.dto.response.UserInfoResponseDto;
import com.sparta.ssaktium.domain.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/internal/users")
public class InternalUserController {

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponseDto> getMyInfo(@AuthenticationPrincipal
                                                          AuthUser authUser) {
        return ResponseEntity.ok(new UserInfoResponseDto(authUser.getUserId()));
    }
}
