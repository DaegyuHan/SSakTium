package com.sparta.ssaktium.config;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class EmailConfig {

    private final JavaMailSender javaMailSender;

    private final String SUBJECT = "[싹틔움] 인증메일 입니다.";

    @Async("emailTaskExecutor")
    public CompletableFuture<Boolean> sendCertificationEmail(String email, String certificationNumber) {

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);

            String htmlContent = getCertificationMessage(certificationNumber);

            messageHelper.setTo(email);
            messageHelper.setSubject(SUBJECT);
            messageHelper.setText(htmlContent, true);

            javaMailSender.send(message);

            log.info("이메일 발신 성공 : {}", email);
            return CompletableFuture.completedFuture(true);

        } catch (Exception e) {
            log.error("이메일 발신 실패 : {}", email, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    private String getCertificationMessage(String certificationNumber) {
        String certificationMessage = "";
        certificationMessage += "<div style='text-align: center;'><img src=\"https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FVbXC4%2FbtsKePF4r6K%2FHdV3AU33uDf8khBSMLMLU0%2Fimg.png\"></div>";
        certificationMessage += "<h1 style='text-align: center;'>[싹틔움] 인증메일</h1>";
        certificationMessage += "<h3 style='text-align: center;'>인증코드: <string style='font-size:32px; letter-spacing: 8px;'>"
                + certificationNumber + "</strong></h3>";
        return certificationMessage;
    }

    @Async("emailTaskExecutor")
    public void sendEmailAsync(int taskId) {
        System.out.println("Task " + taskId + " 시작 - " + Thread.currentThread().getName());
        try {
            // 지연을 주어 비동기 실행을 확인
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Task " + taskId + " 완료 - " + Thread.currentThread().getName());
    }
}