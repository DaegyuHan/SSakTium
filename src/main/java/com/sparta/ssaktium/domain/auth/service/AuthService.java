package com.sparta.ssaktium.domain.auth.service;

import com.sparta.ssaktium.config.JwtUtil;
import com.sparta.ssaktium.domain.auth.dto.request.SigninRequestDto;
import com.sparta.ssaktium.domain.auth.dto.request.SignupRequestDto;
import com.sparta.ssaktium.domain.auth.dto.response.SigninResponseDto;
import com.sparta.ssaktium.domain.auth.dto.response.SignupResponseDto;
import com.sparta.ssaktium.domain.auth.exception.DeletedUserException;
import com.sparta.ssaktium.domain.auth.exception.DuplicateEmailException;
import com.sparta.ssaktium.domain.auth.exception.EmailNotVerifiedException;
import com.sparta.ssaktium.domain.auth.exception.UnauthorizedPasswordException;
import com.sparta.ssaktium.domain.users.entity.User;
import com.sparta.ssaktium.domain.users.enums.UserRole;
import com.sparta.ssaktium.domain.users.enums.UserStatus;
import com.sparta.ssaktium.domain.users.exception.NotFoundUserException;
import com.sparta.ssaktium.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final RedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 회원가입
    public SignupResponseDto signup(SignupRequestDto signupRequestDto) {
        // 이메일 인증 여부 확인
        Boolean isVerified = (Boolean) redisTemplate.opsForValue().get(signupRequestDto.getEmail() + ":verified");

        if (isVerified == null || !isVerified) {
            throw new EmailNotVerifiedException();
        }

        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());

        UserRole userRole = UserRole.of(UserRole.ROLE_USER.getUserRole());

        User newUser = new User(
                signupRequestDto.getEmail(),
                encodedPassword,
                signupRequestDto.getUserName(),
                signupRequestDto.getBirthYear(),
                userRole,
                null
        );
        User savedUser = userRepository.save(newUser);

        String bearerToken = jwtUtil.createAccessToken(savedUser.getId(), savedUser.getEmail(), savedUser.getUserName(), userRole);

        return new SignupResponseDto(bearerToken);
    }

    // 로그인
    public String signin(SigninRequestDto signinRequestDto) {
        User user = userRepository.findByEmail(signinRequestDto.getEmail()).orElseThrow(
                NotFoundUserException::new);

        // 삭제된 사용자에 대한 예외 처리
        if (user.isDeleted()) {
            throw new DeletedUserException();
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(signinRequestDto.getPassword(), user.getPassword())) {
            throw new UnauthorizedPasswordException();
        }

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getUserName(), user.getUserRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        log.info("로그인 성공");
        log.info("access Token : {}", accessToken);
        log.info("refresh Token : {}", refreshToken);

        return accessToken;
    }
}
