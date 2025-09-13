package com.sparta.ssaktium.domain.notifications.service;

import com.sparta.ssaktium.domain.common.service.WebhookService;
import com.sparta.ssaktium.domain.notifications.dto.NotificationMessage;
import com.sparta.ssaktium.domain.notifications.entity.NotificationOutbox;
import com.sparta.ssaktium.domain.notifications.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationOutboxScheduler {

    private final NotificationOutboxRepository outboxRepository;
    private final NotificationProducer notificationProducer;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RETRY_PREFIX = "retry:notification:";
    private static final int MAX_RETRY = 3;
    private final WebhookService webhookService;

//    @Scheduled(fixedDelay = 30000)
    public void sendUnsentMessages() {
        List<NotificationOutbox> messages = outboxRepository
                .findTop50BySentFalseAndErroredFalseOrderByCreatedAtAsc();

        for ( NotificationOutbox outbox : messages ) {
            String retryKey = RETRY_PREFIX + outbox.getId();
            Long retryCount = redisTemplate.opsForValue().increment(retryKey);
            redisTemplate.expire(retryKey, Duration.ofMinutes(3));  // 3 분 TIL

            if ( retryCount != null && retryCount > MAX_RETRY ) {
                log.warn(" 전송 재시도 3회 초과. 알림 전송 - OutboxId: {}", outbox.getId());
                String content = String.format(
                        "**🚨 Kafka 메시지 전송 실패 알림 🚨**\n" +
                                "**Outbox ID**: `%s`\n" +
                                "**User ID**: `%s`\n" +
                                "**Event Type**: `%s`\n" +
                                "**Retry Count**: `%d`\n" +
                                "**시간**: `%s`\n",
                        outbox.getId(),
                        outbox.getUserId(),
                        outbox.getEventType(),
                        retryCount-1,
                        LocalDateTime.now()
                );
                webhookService.sendDiscordNotification(content);
                outbox.markAsErrored();
                redisTemplate.delete(retryKey);
                continue;
            }

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
