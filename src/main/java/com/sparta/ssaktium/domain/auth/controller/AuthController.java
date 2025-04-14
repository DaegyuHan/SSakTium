package com.sparta.ssaktium.domain.auth.controller;

import com.sparta.ssaktium.config.CommonResponse;
import com.sparta.ssaktium.config.JwtUtil;
import com.sparta.ssaktium.domain.auth.dto.request.SigninRequestDto;
import com.sparta.ssaktium.domain.auth.dto.request.SignupRequestDto;
import com.sparta.ssaktium.domain.auth.dto.response.SignupResponseDto;
import com.sparta.ssaktium.domain.auth.service.AuthService;
import com.sparta.ssaktium.domain.common.dto.AuthUser;
import com.sparta.ssaktium.domain.users.entity.User;
import com.sparta.ssaktium.domain.users.enums.UserRole;
import com.sparta.ssaktium.domain.users.repository.UserRepository;
import com.sparta.ssaktium.domain.users.service.RedisUserService;
import com.sparta.ssaktium.domain.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Tag(name = "회원가입/로그인 관리기능", description = "회원가입과 로그인할 수 있는 기능")
public class AuthController {

    private final AuthService authService;
    private final RedisUserService redisUserService;

    @GetMapping("/signin")
    public String signin() {
        return "signin";
    }

    @GetMapping("/ssaktium/signup")
    public String signupPage() {
        return "signup"; // signup.html 페이지를 반환
    }


    @GetMapping("/ssaktium/main")
    public String main() {
        return "main";
    }

    @GetMapping("/ssaktium/my-profile")
    public String myProfile(@AuthenticationPrincipal AuthUser authUser, Model model) {
        model.addAttribute("userName", authUser.getUserName());
        return "my-profile";
    }

    @PostMapping("/v1/auth/signup")
    @Operation(summary = "회원가입", description = "회원가입하는 API")
    @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다.")
    public ResponseEntity<CommonResponse<SignupResponseDto>> signup(@Valid
                                                                    @RequestBody
                                                                    @Parameter(description = "회원정보 입력")
                                                                    SignupRequestDto signupRequestDto) {
        return ResponseEntity.ok(CommonResponse.success(authService.signup(signupRequestDto)));
    }

    @PostMapping("/v1/auth/signin")
    @Operation(summary = "로그인", description = "로그인하는 API")
    @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다.")
    public String signin(@Valid
                         @RequestBody
                         @Parameter(description = "로그인정보 입력")
                         SigninRequestDto signinRequestDto,
                         HttpServletResponse response) {
        String token = authService.signin(signinRequestDto);
        Cookie cookie = new Cookie(JwtUtil.AUTHORIZATION_HEADER, token.substring(7));
        cookie.setPath("/");
        response.addCookie(cookie);

        return "redirect:/ssaktium/main";
    }

    @PostMapping("/v2/auth/logout")
    public String logout(@AuthenticationPrincipal AuthUser authUser, HttpServletRequest request, HttpServletResponse response) {
        redisUserService.deleteRefreshToken(String.valueOf(authUser.getUserId()));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            // SecurityContextLogoutHandler로 로그아웃 처리
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/signin";  // 로그아웃 후 로그인 페이지로 리디렉션
    }
}