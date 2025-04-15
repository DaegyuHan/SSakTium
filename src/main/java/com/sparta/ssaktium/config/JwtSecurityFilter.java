package com.sparta.ssaktium.config;

import com.sparta.ssaktium.domain.common.dto.AuthUser;
import com.sparta.ssaktium.domain.users.enums.UserRole;
import com.sparta.ssaktium.domain.users.service.RedisUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtSecurityFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisUserService redisUserService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest httpRequest,
            @NonNull HttpServletResponse httpResponse,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        //로그인,회원가입 필터 bypass
        String requestURI = httpRequest.getRequestURI();
        if ("/signin".equals(requestURI) || "/signup".equals(requestURI)) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }

        String jwt = null;

        // 헤더에서 Authorization 토큰 가져오기
        String authorizationHeader = httpRequest.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = jwtUtil.substringToken(authorizationHeader);
        } else {
            // 쿠키에서 JWT 토큰 가져오기
            if (httpRequest.getCookies() != null) {
                for (Cookie cookie : httpRequest.getCookies()) {
                    if (JwtUtil.AUTHORIZATION_HEADER.equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // JWT 토큰이 존재할 경우 검증 및 인증
        if (jwt != null) {
            try {
                Claims claims = jwtUtil.extractClaims(jwt);
                long userId = Long.parseLong(claims.getSubject());
                String email = claims.get("email", String.class);
                String userName = claims.get("userName", String.class);
                UserRole userRole = UserRole.of(claims.get("userRole", String.class));

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    AuthUser authUser = new AuthUser(userId, email, userName, userRole);

                    JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(authUser);
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (SecurityException | MalformedJwtException e) {
                log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.", e);
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않는 JWT 서명입니다.");
            } catch (ExpiredJwtException e) {
                log.error("Expired JWT token, 만료된 JWT token 입니다.", e);

                // 만료된 Access Token에서 클레임 추출
                Claims claims = e.getClaims();
                Long userId = Long.parseLong(claims.getSubject());
                String email = claims.get("email", String.class);
                String userName = claims.get("userName", String.class);
                UserRole userRole = UserRole.of(claims.get("userRole", String.class));
                log.info("만료된 Access Token 의 userId : {}", userId);

                // Redis에서 Refresh Token 조회
                String refreshToken = redisUserService.getRefreshToken(userId.toString());
                refreshToken = jwtUtil.substringToken(refreshToken);
                log.info(refreshToken);

                // Refresh Token 유효성 검사

                if (refreshToken != null) {
                    // 새로운 Access Token 발급
                    String newAccessToken = jwtUtil.createAccessToken(userId, email, userName, userRole);

                    // 응답 헤더에 Access Token 추가
                    jwtUtil.addTokenToResponseHeader(newAccessToken, httpResponse);
                    log.info("Access token successfully refreshed for user ID: {}", userId);
                    log.info("new Access Token : {}", newAccessToken);


                    String tokenWithoutBearer = newAccessToken.startsWith("Bearer ") ? newAccessToken.substring(7) : newAccessToken;
                    // 새로운 Access Token을 쿠키에 저장
                    jwtUtil.setTokenCookie(httpResponse, tokenWithoutBearer);

                    // AuthUser 객체 생성
                    AuthUser authUser = new AuthUser(userId, email, userName, userRole);

                    // SecurityContext에 AuthUser 설정
                    Authentication authentication = new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 새로 반영된 헤더로 필터 체인 진행
                    chain.doFilter(httpRequest, httpResponse);
                    return;
                } else {
                    Cookie expiredCookie = new Cookie(JwtUtil.AUTHORIZATION_HEADER, null);
                    expiredCookie.setMaxAge(0);
                    expiredCookie.setPath("/");
                    httpResponse.addCookie(expiredCookie);
                    log.info(":::: refresh token 만료, 로그인 페이지로 redirect ");

                    httpResponse.sendRedirect("/signin");
                    return;
                }


            } catch (UnsupportedJwtException e) {
                log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.", e);
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원되지 않는 JWT 토큰입니다.");
            } catch (Exception e) {
                log.error("Internal server error", e);
                httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        chain.doFilter(httpRequest, httpResponse);
    }

}
