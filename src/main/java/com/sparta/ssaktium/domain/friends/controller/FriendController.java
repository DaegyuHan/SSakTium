package com.sparta.ssaktium.domain.friends.controller;

import com.sparta.ssaktium.config.CommonResponse;
import com.sparta.ssaktium.domain.common.dto.AuthUser;
import com.sparta.ssaktium.domain.friends.dto.responseDto.FriendPageResponseDto;
import com.sparta.ssaktium.domain.friends.dto.responseDto.FriendResponseDto;
import com.sparta.ssaktium.domain.friends.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
@Tag(name = "친구 기능", description = "친구 관련 요청, 승인, 거절, 취소 등 기능")
public class FriendController {

    private final FriendService friendService;

    @GetMapping("/test")
    public ResponseEntity<CommonResponse<String>> testKafka(@AuthenticationPrincipal AuthUser authUser) {
        String responseDto = friendService.testKafka(authUser.getUserId());
        return ResponseEntity.ok(CommonResponse.success(responseDto));
    }

    @PostMapping("/friends/{id}")
    @Operation(summary = "친구 요청", description = "친구 요청할 때 사용하는 API")
    @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다")
    @Parameters({
            @Parameter(name = "id", description = "요청할 유저 ID값", example = "2")
    })
    public ResponseEntity<CommonResponse<FriendResponseDto>> requestFriend(@AuthenticationPrincipal AuthUser authUser,
                                                                           @PathVariable Long id) {
        FriendResponseDto responseDto = friendService.requestFriend(authUser.getUserId(), id);
        return ResponseEntity.ok(CommonResponse.success(responseDto));
    }

    @PutMapping("/friends/{id}")
    @Operation(summary = "친구 수락", description = "친구 수락할 때 사용하는 API")
    @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다.")
    @Parameters({
            @Parameter(name = "id", description = "수락할 유저 ID값", example = "1")
    })
    public ResponseEntity<CommonResponse<FriendResponseDto>> acceptFriend(@AuthenticationPrincipal AuthUser authUser,
                                                                          @PathVariable Long id) {
        FriendResponseDto responseDto = friendService.acceptFriend(authUser.getUserId(), id);
        return ResponseEntity.ok(CommonResponse.success(responseDto));
    }

    @GetMapping("/friends")
    @Operation(summary = "친구조회", description = "친구 조회할 때 사용하는 API")
    @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다.")
    public ResponseEntity<CommonResponse<Page<FriendPageResponseDto>>> getFriends(@AuthenticationPrincipal AuthUser authUser,
                                                                                  @RequestParam(defaultValue = "1") int page,
                                                                                  @RequestParam(defaultValue = "10") int size) {
        Page<FriendPageResponseDto> responseDtos = friendService.getFriends(authUser.getUserId(), page, size);
        return ResponseEntity.ok(CommonResponse.success(responseDtos));
    }


    @DeleteMapping("/friends/{id}")
    @Operation(summary = "친구삭제", description = "친구 요청취소/삭제할 때 사용하는 API")
    @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다.")
    @Parameters({
            @Parameter(name = "id", description = "요청취소/삭제할 유저 ID값", example = "1")
    })
    public ResponseEntity<CommonResponse<Void>> deleteFriend(@AuthenticationPrincipal AuthUser authUser,
                                                             @PathVariable Long id) {
        friendService.deleteFriend(authUser.getUserId(), id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

}
