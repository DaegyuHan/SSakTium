package com.sparta.ssaktium.domain.friends.service;

import com.sparta.ssaktium.domain.friends.dto.responseDto.FriendPageResponseDto;
import com.sparta.ssaktium.domain.friends.dto.responseDto.FriendResponseDto;
import com.sparta.ssaktium.domain.friends.entity.Friend;
import com.sparta.ssaktium.domain.friends.entity.FriendStatus;
import com.sparta.ssaktium.domain.friends.exception.*;
import com.sparta.ssaktium.domain.friends.repository.FriendRepository;
import com.sparta.ssaktium.domain.notifications.dto.EventType;
import com.sparta.ssaktium.domain.notifications.dto.NotificationMessage;
import com.sparta.ssaktium.domain.notifications.service.NotificationProducer;
import com.sparta.ssaktium.domain.users.entity.User;
import com.sparta.ssaktium.domain.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final UserService userService;
    private final FriendRepository friendRepository;
    private final NotificationProducer notificationProducer;

    public String testKafka(Long userId) {
        notificationProducer.sendNotification(
                new NotificationMessage(userId,
                        EventType.FRIEND_REQUESTED,
                        "test"));
        return "카프카 테스트";
    }

    @Transactional
    public FriendResponseDto requestFriend(Long userId, Long friendId) {

        if (Objects.equals(userId, friendId)) {
            throw new SelfRequestException();
        }

        User user = userService.findUser(userId);
        User friendUser = userService.findUser(friendId);

        Optional<Friend> existingFriendRequest =
                friendRepository.findFriendRelationshipBetweenUsers(user.getId(), friendUser.getId());

        if (existingFriendRequest.isPresent()) {
            throw new FriendRequestAlreadySentException();
        }

        Friend newFriend = new Friend(user, friendUser);

        friendRepository.save(newFriend);

        notificationProducer.sendNotification(
                new NotificationMessage(friendId,
                        EventType.FRIEND_REQUESTED,
                        user.getUserName()));

        return new FriendResponseDto(newFriend, newFriend.getUser(), newFriend.getFriendUser());
    }

    @Transactional
    public FriendResponseDto acceptFriend(Long userId, Long friendId) {

        User user = userService.findUser(userId);
        User friendUser = userService.findUser(friendId);

        Friend existingFriendRequest =
                friendRepository.findFriendRelationshipBetweenUsers(userId, friendId)
                        .orElseThrow(NotFoundFriendRequestException::new);

        if (existingFriendRequest.getFriendStatus() == FriendStatus.ACCEPTED) {
            throw new AlreadyFriendsException();
        }

        existingFriendRequest.acceptFriend();
        friendRepository.save(existingFriendRequest);

        notificationProducer.sendNotification(
                new NotificationMessage(friendId,
                        EventType.FRIEND_ACCEPTED,
                        user.getUserName()));

        return new FriendResponseDto(existingFriendRequest, user, friendUser);
    }

    public Page<FriendPageResponseDto> getFriends(Long userId, int page, int size) {

        User user = userService.findUser(userId);

        Page<Friend> friendsPage =
                friendRepository.findFriendsByUserWithStatusOrder(
                        user.getId(),
                        PageRequest.of(page - 1, size
                        )
                );

        return friendsPage.map(friend -> {
            Long friendId = friend.getFriendUser().getId().equals(user.getId())
                    ? friend.getUser().getId()
                    : friend.getFriendUser().getId();
            return new FriendPageResponseDto(friend, friendId);
        });
    }

    @Transactional
    public void deleteFriend(Long userId, Long id) {

        User user = userService.findUser(userId);

        Optional<Friend> friendRelationship = friendRepository.findFriendRelationshipBetweenUsers(user.getId(), id);

        if (friendRelationship.isEmpty()) {
            throw new NotFoundFriendException();
        }

        friendRepository.delete(friendRelationship.get());
    }


    public List<User> findFriends(long userId) {
        return friendRepository.findFriendsByUser(userId, FriendStatus.ACCEPTED);
    }
}
