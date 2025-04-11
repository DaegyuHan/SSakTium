package com.sparta.ssaktium.domain.notifications.service;

import com.sparta.ssaktium.domain.notifications.dto.NotificationMessage;
import com.sparta.ssaktium.domain.notifications.entity.NotificationOutbox;
import com.sparta.ssaktium.domain.notifications.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationOutboxScheduler {

    private final NotificationOutboxRepository outboxRepository;
    private final NotificationProducer notificationProducer;

    @Scheduled(fixedDelay = 5000)
    public void sendUnsentMessages() {
        List<NotificationOutbox> messages = outboxRepository.findTop50BySentFalseOrderByCreatedAtAsc();

        for (NotificationOutbox outbox : messages) {
            try {
                NotificationMessage kafkaMessage = new NotificationMessage(
                        outbox.getUserId(),
                        outbox.getEventType(),
                        outbox.getMessage()
                );

                notificationProducer.sendNotification(kafkaMessage);
                outbox.markAsSent();

                log.info("✅ Kafka 전송 성공 - userId: {}", outbox.getUserId());

            } catch (Exception e) {
                log.error("❌ Kafka 전송 실패 - userId: {}", outbox.getUserId(), e);
            }
        }

        outboxRepository.saveAll(messages);
    }
}
