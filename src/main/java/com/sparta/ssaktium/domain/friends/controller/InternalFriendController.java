package com.sparta.ssaktium.domain.friends.controller;

import com.sparta.ssaktium.domain.friends.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/internal/friends")
public class InternalFriendController {

    private final FriendService friendService;

    // 팔로워 ID 목록만 조회하는 내부 API
    @GetMapping("/{userId}/followers/ids")
    public ResponseEntity<List<Long>> getFollowerIds(@PathVariable Long userId) {
        List<Long> followerIds = friendService.getFollowerIds(userId);
        return ResponseEntity.ok(followerIds);
    }
}
