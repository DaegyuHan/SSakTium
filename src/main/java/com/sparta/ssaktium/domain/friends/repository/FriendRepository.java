package com.sparta.ssaktium.domain.friends.repository;


import com.sparta.ssaktium.domain.friends.entity.FriendStatus;
import com.sparta.ssaktium.domain.friends.entity.Friend;
import com.sparta.ssaktium.domain.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    @Query("SELECT f FROM Friend f " +
            "WHERE (f.user.id = :userId OR f.friendUser.id = :friendId) " +
            "ORDER BY CASE WHEN f.friendStatus = 'PENDING' THEN 0 ELSE 1 END, f.id DESC")
    Page<Friend> findByUserOrFriend(@Param("userId") Long userId,
                                    @Param("friendId") Long friendId,
                                    Pageable pageable);

    @Query("SELECT f FROM Friend f WHERE (f.user.id = :userId AND f.friendUser.id = :friendId) " +
            "OR (f.user.id = :friendId AND f.friendUser.id = :userId)")
    Optional<Friend> findByUserAndFriend(@Param("userId") Long userId,
                                         @Param("friendId") Long friendId);

    @Query("SELECT f.friendUser FROM Friend f WHERE f.user.id = :userId AND f.friendStatus = :status")
    List<User> findFriendsByUser(@Param("userId") Long userId, @Param("status") FriendStatus status);

}