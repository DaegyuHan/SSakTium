package com.sparta.ssaktium.domain.users.controller;

import com.sparta.ssaktium.config.CommonResponse;
import com.sparta.ssaktium.domain.common.dto.AuthUser;
import com.sparta.ssaktium.domain.users.dto.request.UserChangePasswordRequestDto;
import com.sparta.ssaktium.domain.users.dto.request.UserChangeRequestDto;
import com.sparta.ssaktium.domain.users.dto.request.UserCheckPasswordRequestDto;
import com.sparta.ssaktium.domain.users.dto.response.UserImageResponseDto;
import com.sparta.ssaktium.domain.users.dto.response.UserResponseDto;
import com.sparta.ssaktium.domain.users.entity.User;
import com.sparta.ssaktium.domain.users.enums.UserRole;
import com.sparta.ssaktium.domain.users.repository.UserRepository;
import com.sparta.ssaktium.domain.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "유저 기능", description = "유저 관련 조회, 수정, 탈퇴 기능")
public class UserController {

    private final UserService userService;

    @GetMapping("/v1/users/{id}")
    @Operation(summary = "프로필 조회", description = "프로필 조회할 때 사용하는 API")
    @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다")
    public ResponseEntity<CommonResponse<UserResponseDto>> getUser(@PathVariable
                                                                   @Parameter(name = "id", description = "요청할 유저 ID값", example = "1")
                                                                   long id) {
        return ResponseEntity.ok(CommonResponse.success(userService.getUser(id)));
    }

    @PostMapping("/v1/users/change-password")
    @Operation(summary = "유저 비밀번호 수정", description = "내 비밀번호 수정하는 API")
    @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다.")
    public ResponseEntity<CommonResponse<String>> changePassword(@AuthenticationPrincipal
                                                                 AuthUser authUser,
                                                                 @RequestBody
                                                                 @Parameter(description = "이전 비밀번호와 새 비밀번호")
                                                                 UserChangePasswordRequestDto userChangePasswordRequestDto) {
        return ResponseEntity.ok(CommonResponse.success(userService.changePassword(authUser.getUserId(), userChangePasswordRequestDto)));
    }

    @PutMapping("/v1/users/{id}")
    @Operation(summary = "유저 회원정보 수정", description = "유저 회원정보 수정하는 API")
    @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다.")
    public ResponseEntity<CommonResponse<UserResponseDto>> updateUser(@AuthenticationPrincipal
                                                                      AuthUser authUser,
                                                                      @PathVariable
                                                                      @Parameter(description = "요청할 유저 ID값", example = "1")
                                                                      long id,
                                                                      @RequestBody
                                                                      @Parameter(description = "회원정보")
                                                                      UserChangeRequestDto userChangeRequestDto) {
        return ResponseEntity.ok(CommonResponse.success(userService.updateUser(authUser.getUserId(), id, userChangeRequestDto)));
    }

    @PostMapping(value = "/v1/users/update-image")
    @Operation(summary = "유저 프로필 이미지 수정", description = "내 프로필 이미지 수정하는 API")
    @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다.")
    public ResponseEntity<CommonResponse<UserImageResponseDto>> updateDictionary(@RequestPart
                                                                                 @Parameter(description = "프로필 이미지")
                                                                                 MultipartFile image) {
        return ResponseEntity.ok(CommonResponse.success(userService.updateUserImage(image)));
    }

    @DeleteMapping("/v1/users/{id}")
    @Operation(summary = "유저 회원탈퇴", description = "유저 회원탈퇴하는 API")
    @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다.")
    public ResponseEntity<CommonResponse<String>> deleteUser(@AuthenticationPrincipal
                                                             AuthUser authUser,
                                                             @PathVariable
                                                             @Parameter(description = "요청할 유저 ID값", example = "1")
                                                             long id,
                                                             @RequestBody
                                                             @Parameter(description = "비밀번호 확인")
                                                             UserCheckPasswordRequestDto userCheckPasswordRequestDto) {
        return ResponseEntity.ok(CommonResponse.success(userService.deleteUser(authUser.getUserId(), id, userCheckPasswordRequestDto)));
    }
}
