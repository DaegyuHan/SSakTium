package com.sparta.ssaktium.domain.users.customOauth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.ssaktium.config.JwtUtil;
import com.sparta.ssaktium.domain.users.customOauth.dto.CustomOauthInfoDto;
import com.sparta.ssaktium.domain.users.entity.User;
import com.sparta.ssaktium.domain.users.enums.UserRole;
import com.sparta.ssaktium.domain.users.exception.NotFoundUserException;
import com.sparta.ssaktium.domain.users.exception.SocialAccountNoResponseException;
import com.sparta.ssaktium.domain.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOauthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;

    @Value("${app.redirectUri}")
    private String customRedirectUri;


    // 소셜 로그인 서비스를 통해 인증을 수행하는 메서드
    public String socialLogin(String provider, String code, HttpServletResponse response) throws JsonProcessingException {

        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getAccessToken(provider, code);

        // 2. 토큰으로 사용자 정보를 가져오기
        CustomOauthInfoDto UserInfo = fetchUserInfoFromProvider(accessToken, provider);

        log.info(UserInfo.getBirthYear(), UserInfo.getEmail());

        User user = registerUserIfNeeded(UserInfo, response, provider);

        String createToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getUserName(), user.getUserRole());

        log.info(createToken);

        return createToken;
    }


    // AccessToken을 발급받기 위한 공통 메서드
    private String getAccessToken(String provider, String code) {
        String redirectUri = customRedirectUri + provider;
        String url;
        String clientId;
        String clientSecret = null;  // 필요한 경우에만 할당

        // provider 에 따른 URL, clientId, clientSecret 설정
        switch (provider) {
            case "kakao":
                url = "https://kauth.kakao.com/oauth/token";
                clientId = kakaoClientId;
                break;
            case "google":
                url = "https://oauth2.googleapis.com/token";
                clientId = googleClientId;
                clientSecret = googleClientSecret;
                break;
            case "naver":
                url = "https://nid.naver.com/oauth2.0/token";
                clientId = naverClientId;
                clientSecret = naverClientSecret;
                break;
            default:
                throw new NotFoundUserException();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 파라미터 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        // clientSecret 이 존재하는 경우에만 추가
        if (clientSecret != null) {
            params.add("client_secret", clientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        Map responseBody = restTemplate.postForObject(url, request, Map.class);

        if (responseBody == null || !responseBody.containsKey("access_token")) {
            log.error("Failed to fetch access token from {} provider. Response: {}", provider, responseBody);
            throw new NotFoundUserException();
        }

        return (String) responseBody.get("access_token");
    }


    // 소셜 로그인한 사용자 정보로 로그인/회원가입을 처리하는 메서드
    public User registerUserIfNeeded(CustomOauthInfoDto userInfo, HttpServletResponse response, String provider) {
        // 같은 이메일이 있는지 확인
        String changedEmail = userInfo.getEmail() + "_" + provider;
        User existingUser = userRepository.findByEmail(changedEmail).orElse(null);

        if (existingUser == null) {
            // 기존 사용자가 없는 경우 신규 사용자로 등록
            String password = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(password);
            String email = userInfo.getEmail() + "_" + provider;
            String birthYear = userInfo.getBirthYear();
            String socialAccountId = userInfo.getSocialId();

            existingUser = User.builder()
                    .email(email)
                    .userName(userInfo.getUserName())
                    .password(encodedPassword)
                    .birthYear(birthYear)
                    .userRole(UserRole.ROLE_USER)
                    .socialAccountId(socialAccountId)
                    .build();

            userRepository.save(existingUser);

            // JWT 생성 및 헤더 추가
            addJwtToResponse(existingUser, response);

        }
        return existingUser;
    }

    // JWT 토큰을 생성하고, 응답 헤더에 추가하는 메서드
    private void addJwtToResponse(User user, HttpServletResponse response) {
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getUserName(), user.getUserRole());

        jwtUtil.addTokenToResponseHeader(accessToken, response);
    }


    // AccessToken 을 사용하여 소셜 제공자의 사용자 정보를 가져오는 메서드
    private CustomOauthInfoDto fetchUserInfoFromProvider(String accessToken, String provider) throws JsonProcessingException {
        return switch (provider) {
            case "kakao" -> fetchKakaoUserInfo(accessToken);
            case "google" -> fetchGoogleUserInfo(accessToken);
            case "naver" -> fetchNaverUserInfo(accessToken);
            default -> throw new SocialAccountNoResponseException();
        };
    }


    //카카오 사용자 정보를 가져오는 메서드
    private CustomOauthInfoDto fetchKakaoUserInfo(String accessToken) throws JsonProcessingException {
        String url = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>(headers);
        String responseBody = restTemplate.exchange(url, HttpMethod.GET, request, String.class).getBody();

        if (responseBody == null) {
            log.error("Kakao API response body is null.");
            throw new SocialAccountNoResponseException();
        }

        JsonNode jsonNode = new ObjectMapper().readTree(responseBody);
        String socialId = jsonNode.get("id").asText();
        String email = jsonNode.get("kakao_account").get("email").asText();
        String birthYear = jsonNode.get("kakao_account")
                .get("birthyear").asText();
        String userName = jsonNode.get("properties").get("nickname").asText();

        log.info("카카오 사용자 정보: " + socialId + ", " + userName + ", " + birthYear + ", " + email);
        return new CustomOauthInfoDto(socialId, userName, email, birthYear);
    }

    //구글 사용자 정보를 가져오는 메서드
    private CustomOauthInfoDto fetchGoogleUserInfo(String accessToken) throws JsonProcessingException {
        String url = "https://www.googleapis.com/oauth2/v3/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        String responseBody = restTemplate.exchange(url, HttpMethod.GET, request, String.class).getBody();

        if (responseBody == null) {
            log.error("Google API response body is null.");
            throw new SocialAccountNoResponseException();
        }

        JsonNode jsonNode = new ObjectMapper().readTree(responseBody);
        log.info(responseBody);
        String socialId = jsonNode.has("sub") ? jsonNode.get("sub").asText() : null;
        String name = jsonNode.has("name") ? jsonNode.get("name").asText() : null; // null 체크
        String email = jsonNode.has("email") ? jsonNode.get("email").asText() : null; // null 체크

        return CustomOauthInfoDto.addGoogleId(socialId, name, email);
    }

    // 네이버 사용자 정보를 가져오는 메서드
    private CustomOauthInfoDto fetchNaverUserInfo(String accessToken) throws JsonProcessingException {
        String url = "https://openapi.naver.com/v1/nid/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>(headers);
        String responseBody = restTemplate.exchange(url, HttpMethod.GET, request, String.class).getBody();

        if (responseBody == null) {
            log.error("Naver API response body is null.");
            throw new SocialAccountNoResponseException();
        }

        JsonNode jsonNode = new ObjectMapper().readTree(responseBody);
        JsonNode responseNode = jsonNode.get("response");
        String socialId = responseNode.has("id") ? responseNode.get("id").asText() : null; // null 체크
        String email = responseNode.has("email") ? responseNode.get("email").asText() : null; // null 체크
        String userName = responseNode.has("name") ? responseNode.get("name").asText() : null; // null 체크
        String birthyear = responseNode.has("birthyear") ? responseNode.get("birthyear").asText() : null; // null 체크

        log.info("네이버 사용자 정보: " + ", " + userName + ", " + birthyear + ", " + email);
        return new CustomOauthInfoDto(socialId, userName, email, birthyear);
    }
}
