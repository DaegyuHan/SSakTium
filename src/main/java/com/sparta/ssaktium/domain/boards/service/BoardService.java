package com.sparta.ssaktium.domain.boards.service;

import com.sparta.ssaktium.domain.boards.dto.requestDto.BoardSaveRequestDto;
import com.sparta.ssaktium.domain.boards.dto.responseDto.BoardDetailResponseDto;
import com.sparta.ssaktium.domain.boards.dto.responseDto.BoardSaveResponseDto;
import com.sparta.ssaktium.domain.boards.dto.responseDto.BoardUpdateImageDto;
import com.sparta.ssaktium.domain.boards.entity.Board;
import com.sparta.ssaktium.domain.boards.entity.BoardImages;
import com.sparta.ssaktium.domain.boards.enums.PublicStatus;
import com.sparta.ssaktium.domain.boards.enums.StatusEnum;
import com.sparta.ssaktium.domain.boards.exception.InvalidBoardTypeException;
import com.sparta.ssaktium.domain.boards.exception.NotFoundBoardException;
import com.sparta.ssaktium.domain.boards.exception.NotUserOfBoardException;
import com.sparta.ssaktium.domain.boards.repository.BoardImagesRepository;
import com.sparta.ssaktium.domain.boards.repository.BoardRepository;
import com.sparta.ssaktium.domain.common.service.S3Service;
import com.sparta.ssaktium.domain.friends.service.FriendService;
import com.sparta.ssaktium.domain.likes.LikeRedisService;
import com.sparta.ssaktium.domain.notifications.dto.EventType;
import com.sparta.ssaktium.domain.notifications.dto.NotificationMessage;
import com.sparta.ssaktium.domain.notifications.service.NotificationProducer;
import com.sparta.ssaktium.domain.users.entity.User;
import com.sparta.ssaktium.domain.users.enums.UserRole;
import com.sparta.ssaktium.domain.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserService userService;
    private final FriendService friendService;
    private final S3Service s3Service;
    private final BoardImagesRepository boardImagesRepository;
    private final LikeRedisService likeRedisService; // 좋아요 수 반영을 위함
    private final NotificationProducer notificationProducer;


    @Transactional
    public BoardSaveResponseDto saveBoards(Long userId,
                                           BoardSaveRequestDto requestDto,
                                           List<MultipartFile> imageList) {
        //유저 확인
        User user = userService.findUser(userId);

        Board board = new Board(requestDto.getTitle(),
                requestDto.getContents(),
                requestDto.getPublicStatus(),
                user);

        // 업로드한 파일의 S3 URL 주소
        List<String> imageUrls = s3Service.uploadImageListToS3(imageList, s3Service.bucket);

        //저장
        Board savedBoard = boardRepository.save(board);
        //boardimages에 저장
        for (String imageUrl : imageUrls) {
            BoardImages boardImage = new BoardImages(imageUrl, savedBoard);// Board 설정
            boardImagesRepository.save(boardImage); // BoardImagesRepository에 저장
        }

        // 친구 관계인 모든 유저에게 알림 발송
        List<User> friends = friendService.findFriends(userId);
        friends.forEach(friend -> notificationProducer.sendNotification(
                new NotificationMessage(friend.getId(),
                        EventType.FRIEND_BOARD,
                        user.getUserName() + "님이 게시글"+ requestDto.getTitle() + "을 등록했습니다."))
        );

        //responseDto 반환
        return new BoardSaveResponseDto(savedBoard, imageUrls);
    }

    @Transactional
    public BoardUpdateImageDto updateImages(Long userId,
                                            Long id,
                                            List<MultipartFile> imageList,
                                            List<String> remainingImages) {
        //유저 확인
        User user = userService.findUser(userId);
        //게시글 찾기
        Board board = getBoardById(id);
        //게시글 본인 확인
        if (!board.getUser().equals(user)) {
            throw new NotUserOfBoardException();
        }
        // 기존 BoardImages 삭제 (DB 및 S3에서)
        List<BoardImages> existingImages = board.getImageUrls();
        for (BoardImages boardImage : existingImages) {
            if (!remainingImages.contains(boardImage.getImageUrl())) {
                s3Service.deleteObject(s3Service.bucket, boardImage.getImageUrl()); // S3에서 이미지 삭제
            }
        }
        //boardImages에 해당 보드 id를 가진 항목 삭제
        boardImagesRepository.deleteByBoardId(id);

        // 기존 이미지 이름을 유지하고, 새 이미지만 업로드
        List<String> updatedImageList = new ArrayList<>(remainingImages);
        //새로 추가하는 이미지가 비어있을 경우 작동 x
        if (!imageList.isEmpty()) {
            for (MultipartFile image : imageList) {
                if (!image.isEmpty()) {//비어있지 않은 파일만  처리
                    String originalFileName = image.getOriginalFilename();
                    // 이미 존재하는 파일 이름을 유지
                    if (!updatedImageList.contains(originalFileName)) {
                        String newImageUrl = s3Service.uploadImageToS3(image, s3Service.bucket);
                        updatedImageList.add(newImageUrl);
                    }
                }
            }
        }

        // BoardImages로 변환 후 저장
        List<BoardImages> newBoardImages = updatedImageList.stream()
                .map(imageUrl -> new BoardImages(imageUrl, board)) // 필요한 경우 추가 필드 설정
                .toList();

        //게시글 수정
        boardImagesRepository.saveAll(newBoardImages);
        //responseDto 반환
        return new BoardUpdateImageDto(updatedImageList);
    }

    @Transactional
    public BoardSaveResponseDto updateBoardContent(Long userId,
                                                   Long id,
                                                   BoardSaveRequestDto requestDto) {
        // 유저 확인
        User user = userService.findUser(userId);
        // 게시글 찾기
        Board board = getBoardById(id);
        // 게시글 본인 확인
        if (!board.getUser().equals(user)) {
            throw new NotUserOfBoardException();
        }

        // 기존 데이터 유지
        String title = requestDto.getTitle() != null ? requestDto.getTitle() : board.getTitle();
        String content = requestDto.getContents() != null ? requestDto.getContents() : board.getContent();
        PublicStatus publicStatus = requestDto.getPublicStatus() != null ? requestDto.getPublicStatus() : board.getPublicStatus();

        // 게시글 수정
        board.updateBoards(title, content, publicStatus); // 이미지 리스트는 그대로 유지
        Board updatedBoard = boardRepository.save(board);
        // 기존의 BoardImages에서 imageUrl만 추출하여 문자열 리스트로 변환
        List<String> imageUrls = updatedBoard.getImageUrls().stream()
                .map(BoardImages::getImageUrl) // 각 BoardImages의 imageUrl만 가져옴
                .toList();
        // responseDto 반환
        return new BoardSaveResponseDto(updatedBoard, imageUrls);
    }

    @Transactional
    public void deleteBoards(Long userId, Long id) {
        //유저 확인
        User user = userService.findUser(userId);
        //게시글 찾기
        Board board = getBoardById(id);
        //어드민 일시 본인 확인 넘어가기
        if (!user.getUserRole().equals(UserRole.ROLE_ADMIN)) {
            //게시글 본인 확인
            if (!board.getUser().equals(user)) {
                throw new NotUserOfBoardException();
            }
        }

        List<String> imageUrls = board.getImageUrls().stream()
                .map(BoardImages::getImageUrl)
                .toList();
        // 기존 등록된 URL 가지고 이미지 원본 이름 가져오기
        List<String> deletedImages = s3Service.extractFileNamesFromUrls(imageUrls);
        //가져온 이미지 리스트 삭제
        for (String imageurl : deletedImages) {
            s3Service.deleteObject(s3Service.bucket, imageurl); // 반복적으로 삭제
        }
        //해당 보드 삭제 상태 변경
        board.deleteBoards();
        boardRepository.save(board);
    }

    //게시글 단건 조회
    public BoardDetailResponseDto getBoard(Long id) {
        //게시글 찾기
        Board board = boardRepository.findByIdAndStatusEnum(id, StatusEnum.ACTIVATED).orElseThrow(NotFoundBoardException::new);
        // 댓글 수 가져오기
        int commentCount = boardRepository.countCommentsByBoardId(board.getId());
        //boardimage url만 가져오기
        List<String> imageUrls = board.getImageUrls().stream()
                .map(BoardImages::getImageUrl) // BoardImages에서 URL 추출
                .toList();
        // 좋아요 수 레디스에서 반영
        int redisLikeCount = getLikeCount(id.toString());
        return new BoardDetailResponseDto(board, imageUrls, commentCount, redisLikeCount);
    }

    public Page<BoardDetailResponseDto> getBoards(Long userId, String type, int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size);
        List<BoardDetailResponseDto> boardDetails = new ArrayList<>();

        if ("me".equalsIgnoreCase(type)) {
            // 사용자가 쓴 게시글 조회
            User user = userService.findUser(userId);
            Page<Board> boards = boardRepository.findAllByUserIdAndStatusEnum(user.getId(), StatusEnum.ACTIVATED, pageable);

            for (Board board : boards) {
                int commentCount = boardRepository.countCommentsByBoardId(board.getId());
                List<String> imageUrls = board.getImageUrls().stream()
                        .map(BoardImages::getImageUrl) // BoardImages에서 URL 추출
                        .toList();
                // 좋아요 수 레디스에서 반영
                int redisLikeCount = getLikeCount(board.getId().toString());
                boardDetails.add(new BoardDetailResponseDto(board, imageUrls, commentCount, redisLikeCount));
            }
            return new PageImpl<>(boardDetails, pageable, boards.getTotalElements());

        } else if ("all".equalsIgnoreCase(type)) {
            // 전체 공개 게시글 조회
            Page<Board> boardsPage = boardRepository.findAllByPublicStatus(PublicStatus.ALL, pageable);

            for (Board board : boardsPage.getContent()) {
                int commentCount = boardRepository.countCommentsByBoardId(board.getId());
                List<String> imageUrls = board.getImageUrls().stream()
                        .map(BoardImages::getImageUrl) // BoardImages에서 URL 추출
                        .toList();
                // 좋아요 수 레디스에서 반영
                int redisLikeCount = getLikeCount(board.getId().toString());
                boardDetails.add(new BoardDetailResponseDto(board, imageUrls, commentCount, redisLikeCount));
            }
            return new PageImpl<>(boardDetails, pageable, boardsPage.getTotalElements());
        } else {
            throw new InvalidBoardTypeException();
        }
    }

    //뉴스피드
    public Page<BoardDetailResponseDto> getNewsfeed(Long userId, int page, int size) {
        //사용자 찾기
        User user = userService.findUser(userId);
        Pageable pageable = PageRequest.of(page - 1, size);

        //친구목록 가져오기
        List<User> friends = friendService.findFriends(user.getId());

        // 게시글 리스트 가져오기
        Page<Board> boardsPage = boardRepository.findAllForNewsFeed(
                user,
                friends,
                PublicStatus.FRIENDS, // 친구의 게시글 상태
                PublicStatus.ALL,// 친구의 전체 게시글 상태
                pageable               // Pageable 객체 전달
        );

        List<BoardDetailResponseDto> dtoList = new ArrayList<>();
        for (Board board : boardsPage) {
            // 댓글 리스트 가져오기
            int commentCount = boardRepository.countCommentsByBoardId(board.getId());
            //image url만 가져오기
            List<String> imageUrls = board.getImageUrls().stream()
                    .map(BoardImages::getImageUrl) // BoardImages에서 URL 추출
                    .toList();
            // 좋아요 수 레디스에서 반영
            int redisLikeCount = getLikeCount(board.getId().toString());
            // 댓글 리스트 대신 댓글 수만 포함하는 DTO 생성
            dtoList.add(new BoardDetailResponseDto(board, imageUrls, commentCount, redisLikeCount));
        }
        return new PageImpl<>(dtoList, pageable, boardsPage.getTotalElements());
    }

    //Board 찾는 메서드
    public Board getBoardById(Long id) {
        return boardRepository.findByIdAndStatusEnum(id, StatusEnum.ACTIVATED)
                .orElseThrow(NotFoundBoardException::new);
    }

    // Redis 로 좋아요 수 조회하는 메서드
    public int getLikeCount(String targetId) {
        int redisCount = likeRedisService.getRedisLikeCount("Board", targetId);
        if (redisCount == 0) {
            return boardRepository.findBoardLikesCountById(Long.parseLong(targetId));
        }
        return redisCount;
    }
}
