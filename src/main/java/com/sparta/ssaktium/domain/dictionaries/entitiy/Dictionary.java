package com.sparta.ssaktium.domain.dictionaries.entitiy;

import com.sparta.ssaktium.domain.dictionaries.dto.request.DictionaryUpdateRequestDto;
import com.sparta.ssaktium.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.tools.Diagnostic;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "dictionaries")
public class Dictionary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Dictionary(String title, String content, User user, String imageUrl) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.imageUrl = imageUrl;
    }

    // 식물도감 생성
    public static Dictionary addDictionary(String title, String content, User user, String imageUrl) {
        return new Dictionary(title, content, user, imageUrl);
    }

    // 식물도감 수정
    public void update(DictionaryUpdateRequestDto dictionaryUpdateRequestDto) {
        this.title = dictionaryUpdateRequestDto.getTitle();
        this.content = dictionaryUpdateRequestDto.getContent();
        this.imageUrl = dictionaryUpdateRequestDto.getProfileImageUrl();
    }
}
