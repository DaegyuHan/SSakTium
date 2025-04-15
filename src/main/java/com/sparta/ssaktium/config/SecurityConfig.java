package com.sparta.ssaktium.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final JwtSecurityFilter jwtSecurityFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtSecurityFilter, SecurityContextHolderAwareRequestFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutUrl("/v2/auth/logout") // 로그아웃 URL
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setHeader("Authorization", "");
                            response.addCookie(new Cookie("Authorization", "") {{
                                setMaxAge(0);
                                setPath("/");
                            }});
                            response.sendRedirect("/signin"); // 로그아웃 후 로그인 페이지로 리디렉션
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v1/auth/**",
                                "/v2/auth/**",
                                "/v2/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/signin/**", // 로그인 접근 허용
                                "/signin/*",
                                "/signin",
                                "/ssaktium/signup", // 회원가입 접근 허용
                                "/api/v1/query",
                                "/actuator/*",
                                "/v1/api/internal/**"

                        ).permitAll()
                        .requestMatchers("/ssaktium/main").authenticated() // 인증 필요 경로
                        .anyRequest().authenticated()
                )
                .build();
    }

    private AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) -> {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(
                    "<script>alert('로그인이 필요합니다.'); location.href='/signin';</script>"
            );
        };
    }
}
