package com.sparta.ssaktium.domain.likes;

import com.sparta.ssaktium.domain.likes.boardLikes.repository.BoardLikeRepository;
import com.sparta.ssaktium.domain.likes.commentLikes.repository.CommentLikeRepository;
import lombok.Getter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Getter
@Component
public class LikeRedisService {

    public static final String TARGET_TYPE_BOARD = "board";
    public static final String TARGET_TYPE_COMMENT = "comment";
    private final RedisTemplate<String, Object> redisTemplate; // RedisTemplate 사용
    private final BoardLikeRepository boardLikeRepository;
    private final CommentLikeRepository commentLikeRepository;

    public LikeRedisService(BoardLikeRepository boardLikeRepository,
                            RedisTemplate<String, Object> redisTemplate,
                            CommentLikeRepository commentLikeRepository) {
        this.boardLikeRepository = boardLikeRepository;
        this.redisTemplate = redisTemplate;
        this.commentLikeRepository = commentLikeRepository;
    }

    // 좋아요 수 증가
    @Transactional
    public void incrementLike(String targetType, String targetId, String userId) {
        String likeKey = "likes:" + targetType + ":" + targetId;
        String userLikeKey = targetType + "_likes:" + targetId;

        redisTemplate.opsForValue().increment(likeKey, 1);  // 좋아요 수 증가
        redisTemplate.opsForSet().add(userLikeKey, userId);  // 사용자 좋아요 기록 추가
    }

    // 좋아요 수 감소
    @Transactional
    public void decrementLike(String targetType, String targetId, String userId) {
        String likeKey = "likes:" + targetType + ":" + targetId;
        String userLikeKey = targetType + "_likes:" + targetId;

        redisTemplate.opsForValue().decrement(likeKey, 1);  // 좋아요 수 감소
        redisTemplate.opsForSet().remove(userLikeKey, userId);  // 사용자 좋아요 기록 제거
    }

    // 중복 좋아요 확인
    public boolean isLiked(String targetType, String targetId, String userId) {
        String userLikeKey = targetType + "_likes:" + targetId;
        return redisTemplate.opsForSet().isMember(userLikeKey, userId);
    }

    // 좋아요 수 조회
    public int getRedisLikeCount(String targetType, String targetId) {
        String likeKey = "likes:" + targetType + ":" + targetId;
        String userLikeKey = targetType + "_likes:" + targetId;
        Integer count = (Integer) redisTemplate.opsForValue().get(likeKey);

        // 캐시가 없을 경우 DB 에서 조회 후 저장
        if (count == null) {
            int dbLikeCount;
            List<String> likedUsers;
            if (targetType.equals(TARGET_TYPE_BOARD)) {
                dbLikeCount = boardLikeRepository.countByBoardId(Long.parseLong(targetId));
                likedUsers = boardLikeRepository.findUserIdsByBoardId(Long.parseLong(targetId));
            } else if (targetType.equals(TARGET_TYPE_COMMENT)) {
                dbLikeCount = commentLikeRepository.countByCommentId(Long.parseLong(targetId));
                likedUsers = commentLikeRepository.findUserIdsByCommentId(Long.parseLong(targetId));
            } else {
                throw new IllegalArgumentException("지원하지 않는 대상 타입: " + targetType);
            }
            // 좋아요 수 뿐만 아니라 중복 체크를 위한 데이터도 포함해야함.!! 자고 일어나서 할 것.
            redisTemplate.opsForValue().set(likeKey, dbLikeCount);
            for (String userId : likedUsers){
                redisTemplate.opsForSet().add(userLikeKey,userId);
            }
            return dbLikeCount;
        }
        return count;
    }
}
