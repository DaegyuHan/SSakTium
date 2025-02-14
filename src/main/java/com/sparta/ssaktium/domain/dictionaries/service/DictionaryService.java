package com.sparta.ssaktium.domain.dictionaries.service;

import com.sparta.ssaktium.domain.common.service.S3Service;
import com.sparta.ssaktium.domain.dictionaries.dto.request.DictionaryRequestDto;
import com.sparta.ssaktium.domain.dictionaries.dto.request.DictionaryUpdateRequestDto;
import com.sparta.ssaktium.domain.dictionaries.dto.response.DictionaryImageResponseDto;
import com.sparta.ssaktium.domain.dictionaries.dto.response.DictionaryListResponseDto;
import com.sparta.ssaktium.domain.dictionaries.dto.response.DictionaryResponseDto;
import com.sparta.ssaktium.domain.dictionaries.entitiy.Dictionary;
import com.sparta.ssaktium.domain.dictionaries.exception.NotFoundDictionaryException;
import com.sparta.ssaktium.domain.dictionaries.repository.DictionaryRepository;
import com.sparta.ssaktium.domain.users.entity.User;
import com.sparta.ssaktium.domain.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DictionaryService {

    private final DictionaryRepository dictionaryRepository;
    private final UserService userService;
    private final S3Service s3Service;

    // 식물도감 등록
    @Transactional
    public DictionaryResponseDto createDictionary(long userId, DictionaryRequestDto dictionaryRequestDto, MultipartFile image){
        // 유저 조회
        User user = userService.findUser(userId);

        // 업로드한 파일의 S3 URL 주소
        String imageUrl = s3Service.uploadImageToS3(image, s3Service.bucket);

        // Entity 생성
        Dictionary dictionary = Dictionary.addDictionary(dictionaryRequestDto.getTitle(), dictionaryRequestDto.getContent(), user, imageUrl);

        // DB 저장
        Dictionary savedDictionary = dictionaryRepository.save(dictionary);

        // Dto 반환
        return new DictionaryResponseDto(savedDictionary);
    }

    // 식물도감 단건 조회
    public DictionaryResponseDto getDictionary(long dictionaryId) {

        // 식물도감 조회
        Dictionary dictionary = findDictionary(dictionaryId);

        // Dto 반환
        return new DictionaryResponseDto(dictionary);
    }

    // 식물도감 리스트 조회
    public Page<DictionaryListResponseDto> getDictionaryList(int page, int size) {

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page - 1, size);

        // 식물도감 전체 조회
        Page<Dictionary> dictionariesPage = dictionaryRepository.findAll(pageable);

        // Dto 변환
        return dictionariesPage.map(dictionary -> new DictionaryListResponseDto(dictionary.getTitle()));
    }

    // 식물도감 수정
    @Transactional
    public DictionaryResponseDto updateDictionary(DictionaryUpdateRequestDto dictionaryUpdateRequestDto, long dictionaryId){

        // 식물도감 조회
        Dictionary dictionary = findDictionary(dictionaryId);

        // Entity 수정
        dictionary.update(dictionaryUpdateRequestDto);

        // DB 저장
        dictionaryRepository.save(dictionary);

        // DTO 반환
        return new DictionaryResponseDto(dictionary);
    }

    // 식물도감 이미지 변경
    @Transactional
    public DictionaryImageResponseDto updateDictionaryImage(long dictionaryId,  MultipartFile image) {

        // 식물도감 조회
        findDictionary(dictionaryId);

        // 업로드한 파일의 S3 URL 주소
        String imageUrl = s3Service.uploadImageToS3(image, s3Service.bucket);

        // DTO 반환
        return new DictionaryImageResponseDto(imageUrl);
    }

    // 식물도감 삭제
    @Transactional
    public String deleteDictionary(long dictionaryId) {

        // 식물도감 조회
        Dictionary dictionary = findDictionary(dictionaryId);

        // 기존 등록된 URL 가지고 이미지 원본 이름 가져오기
        String imageName = s3Service.extractFileNameFromUrl(dictionary.getImageUrl());

        // 가져온 이미지 원본 이름으로 S3 이미지 삭제
        s3Service.deleteObject(s3Service.bucket, imageName);

        // DB 삭제
        dictionaryRepository.delete(dictionary);

        return "정상적으로 삭제되었습니다.";
    }

    // 식물도감 조회 메서드
    public Dictionary findDictionary(long dictionaryId) {
        return dictionaryRepository.findById(dictionaryId).orElseThrow(NotFoundDictionaryException::new);
    }
}
