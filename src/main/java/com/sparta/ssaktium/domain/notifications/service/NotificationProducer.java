package com.sparta.ssaktium.domain.notifications.service;

import com.sparta.ssaktium.domain.notifications.dto.NotificationMessage;
import com.sparta.ssaktium.domain.notifications.entity.NotificationOutbox;
import com.sparta.ssaktium.domain.notifications.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NotificationOutboxRepository notificationOutboxRepository;

    public void sendNotification(NotificationMessage message) {
        kafkaTemplate.send("notifications", message);
    }

    @Transactional
    public void sendWithOutbox(NotificationMessage message) {
        NotificationOutbox outbox = new NotificationOutbox(
                message.getUserId(),
                message.getEventType(),
                message.getMessage()
        );

        try {
            if (true) {
                throw new RuntimeException("💥 고의로 발생시킨 Kafka 예외");
            }
            kafkaTemplate.send("notifications", message);
            log.info("✅ Kafka 전송 성공");
        } catch (Exception e) {
            log.error("❌ Kafka 전송 실패, Outbox 스케줄러가 재전송할 예정", e);
            outbox.markAsUnsent(); // 전송 실패 시 상태 변경
        }

        notificationOutboxRepository.save(outbox);
    }

}
