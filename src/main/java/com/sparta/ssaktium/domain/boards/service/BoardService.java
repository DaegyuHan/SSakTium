package com.sparta.ssaktium.domain.boards.service;

import com.sparta.ssaktium.domain.boards.dto.requestDto.BoardSaveRequestDto;
import com.sparta.ssaktium.domain.boards.dto.responseDto.BoardDetailResponseDto;
import com.sparta.ssaktium.domain.boards.dto.responseDto.BoardSaveResponseDto;
import com.sparta.ssaktium.domain.boards.dto.responseDto.BoardSearchResponseDto;
import com.sparta.ssaktium.domain.boards.dto.responseDto.BoardUpdateImageDto;
import com.sparta.ssaktium.domain.boards.entity.Board;
import com.sparta.ssaktium.domain.boards.entity.BoardDocument;
import com.sparta.ssaktium.domain.boards.entity.BoardImages;
import com.sparta.ssaktium.domain.boards.enums.PublicStatus;
import com.sparta.ssaktium.domain.boards.exception.InvalidBoardTypeException;
import com.sparta.ssaktium.domain.boards.exception.NotFoundBoardException;
import com.sparta.ssaktium.domain.boards.exception.NotUserOfBoardException;
import com.sparta.ssaktium.domain.boards.repository.BoardImagesRepository;
import com.sparta.ssaktium.domain.boards.repository.BoardRepository;
import com.sparta.ssaktium.domain.boards.repository.BoardSearchRepository;
import com.sparta.ssaktium.domain.common.service.S3Service;
import com.sparta.ssaktium.domain.friends.service.FriendService;
import com.sparta.ssaktium.domain.likes.LikeRedisService;
import com.sparta.ssaktium.domain.likes.boardLikes.repository.BoardLikeRepository;
import com.sparta.ssaktium.domain.notifications.dto.EventType;
import com.sparta.ssaktium.domain.notifications.dto.NotificationMessage;
import com.sparta.ssaktium.domain.notifications.service.NotificationOutboxService;
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
    private final BoardSearchRepository boardSearchRepository;
    private final LikeRedisService likeRedisService; // 좋아요 수 반영을 위함
    private final NotificationProducer notificationProducer;
    private final BoardLikeRepository boardLikeRepository;
    private final NotificationOutboxService notificationOutboxService;


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
        List<String> imageUrls = (imageList != null && !imageList.isEmpty())
                ? s3Service.uploadImageListToS3(imageList, s3Service.bucket) : new ArrayList<>();

        //저장
        Board savedBoard = boardRepository.save(board);
        //boardimages에 저장
        if (!imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                BoardImages boardImage = new BoardImages(imageUrl, savedBoard);// Board 설정
                boardImagesRepository.save(boardImage); // BoardImagesRepository에 저장
            }
        }
        //elastic에 저장할 document 생성
        BoardDocument boardDocument = new BoardDocument(
                savedBoard.getId(),
                savedBoard.getTitle(),
                savedBoard.getContent(),
                String.join(",", imageUrls));
        //elastic에 저장
        boardSearchRepository.save(boardDocument);

        // 알림 전송
        notificationProducer.sendWithOutbox(
                new NotificationMessage(
                        user.getId(),
                        EventType.POST_CREATED,
                        requestDto.getTitle())
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

        for (MultipartFile image : imageList) {
            String originalFileName = image.getOriginalFilename();
            if (!image.isEmpty()) {
                // 이미 존재하는 파일 이름을 유지
                if (!updatedImageList.contains(originalFileName)) {
                    String newImageUrl = s3Service.uploadImageToS3(image, s3Service.bucket);
                    updatedImageList.add(newImageUrl);
                }
            }
        }


        // BoardImages로 변환 후 저장
        List<BoardImages> newBoardImages = updatedImageList.stream()
                .map(imageUrl -> new BoardImages(imageUrl, board)) // 필요한 경우 추가 필드 설정
                .toList();

        //게시글 수정
        boardImagesRepository.saveAll(newBoardImages);

        //elasticsearch에 이미지 변경값 저장
        BoardDocument boardDocument = new BoardDocument(
                board.getId(),
                board.getTitle(),
                board.getContent(),
                String.join(",", updatedImageList));

        boardSearchRepository.save(boardDocument);
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

        //elasticsearch에 본문 변경값 저장
        BoardDocument boardDocument = new BoardDocument(
                updatedBoard.getId(),
                updatedBoard.getTitle(),
                updatedBoard.getContent(),
                String.join(",", imageUrls));
        boardSearchRepository.save(boardDocument);
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
        // 엘라스틱서치에서 게시글 문서 삭제
        boardSearchRepository.deleteById(id);

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
        boardRepository.delete(board);
    }

    //게시글 단건 조회
    public BoardDetailResponseDto getBoard(Long id) {
        //게시글 찾기
        Board board = boardRepository.findById(id).orElseThrow(NotFoundBoardException::new);
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
            Page<Board> boards = boardRepository.findAllByUserId(user.getId(), pageable);

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

    public Page<BoardSearchResponseDto> searchBoard(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);  // 페이지 번호, 페이지 크기
        Page<Board> boardPage = boardRepository.searchBoardByTitleOrContent(keyword, pageable);

        // Page<Board>를 Page<BoardSearchResponseDto>로 변환
        return boardPage.map(BoardSearchResponseDto::new);
    }

    public Page<BoardDocument> elasticsearch(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);  // page - 1 처리

        // ElasticsearchRepository에서 제공하는 메서드를 사용하여 검색
        return boardSearchRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
    }

    //Board 찾는 메서드
    public Board getBoardById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(NotFoundBoardException::new);
    }

    // Redis 로 좋아요 수 조회하는 메서드
    public int getLikeCount(String boardId) {
        int redisCount = likeRedisService.getRedisLikeCount(likeRedisService.TARGET_TYPE_BOARD, boardId);
        if (redisCount == 0) {
            return boardLikeRepository.countByBoardId(Long.valueOf(boardId));
        }
        return redisCount;
    }
}
