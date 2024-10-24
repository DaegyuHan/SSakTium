package com.sparta.ssaktium.domain.users.repository;

import com.sparta.ssaktium.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByKakaoId(Long kakaoId);
}