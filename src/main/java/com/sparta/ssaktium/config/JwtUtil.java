package com.sparta.ssaktium.config;

import com.sparta.ssaktium.domain.users.enums.UserRole;
import com.sparta.ssaktium.domain.users.service.RedisUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
@RequiredArgsConstructor
public class JwtUtil {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final long TOKEN_TIME = 15 * 60 * 1000L; // 15분
    private static final long REFRESH_TOKEN_TIME = 24 * 60 * 60 * 1000L; // 1일

    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    private final RedisUserService redisUserService;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // Access Token 생성
    public String createAccessToken(Long userId, String email, String userName, UserRole userRole) {
        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(String.valueOf(userId))
                        .claim("email", email)
                        .claim("userName", userName)
                        .claim("userRole", userRole)
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME))
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                        .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(Long userId) {
        Date date = new Date();

        String refreshToken =  BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(String.valueOf(userId))
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME))
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm)
                        .compact();

        redisUserService.saveRefreshToken(userId.toString(), refreshToken, REFRESH_TOKEN_TIME);
        return refreshToken;
    }

    // Bearer 분리 메서드
    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        } else {
            return null;
        }
    }

    // Token Claim 메서드
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void addTokenToResponseHeader(String token, HttpServletResponse response) {
        response.addHeader("Authorization", token);
    }

    // Access Token을 쿠키에 저장 (Bearer prefix 없이)
    public void setTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(AUTHORIZATION_HEADER, token);
        cookie.setHttpOnly(true);  // JavaScript에서 접근할 수 없도록 설정
        cookie.setSecure(true);     // HTTPS 프로토콜에서만 전송
        cookie.setPath("/");       // 모든 경로에서 접근 가능
        cookie.setMaxAge(60 * 60); // 1시간 동안 유효
        response.addCookie(cookie);
    }
}
