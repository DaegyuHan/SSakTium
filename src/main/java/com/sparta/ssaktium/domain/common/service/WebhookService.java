package com.sparta.ssaktium.domain.common.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookService {

    @Value("${spring.webhook.discord-url}")
    private String webhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendDiscordNotification(String template, Object...args) {

        String message = String.format(template,args);

        Map<String, String> body = new HashMap<>();
        body.put("content", message);

        // HTTP 요청 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        // Webhook 호출
        ResponseEntity<String> response = restTemplate.exchange(
                webhookUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Discord 알림 전송 성공!");
        } else {
            System.out.println("Discord 알림 전송 실패: " + response.getBody());
        }
    }
}
