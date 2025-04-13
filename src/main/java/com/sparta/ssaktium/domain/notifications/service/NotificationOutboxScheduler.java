package com.sparta.ssaktium.domain.notifications.service;

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
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationOutboxScheduler {

    private final NotificationOutboxRepository outboxRepository;
    private final NotificationProducer notificationProducer;
    private final RedisTemplate<String, Object> redisTemplate;

    private final String RETRY_PREFIX = "retry:notification:";
    private static final int MAX_RETRY = 3;

    @Scheduled(fixedDelay = 15000)
    public void sendUnsentMessages() {
        List<NotificationOutbox> messages = outboxRepository
                .findTop50BySentFalseAndDlqFalseOrderByCreatedAtAsc();

        for ( NotificationOutbox outbox : messages ) {
            String retryKey = RETRY_PREFIX + outbox.getId();
            Long retryCount = redisTemplate.opsForValue().increment(retryKey);
            redisTemplate.expire(retryKey, Duration.ofMinutes(3));  // 3 분 TIL

            if ( retryCount != null && retryCount > MAX_RETRY ) {
                log.warn(" 전송 재시도 {}회 초과. DLQ 처리 - OutboxId: {}", retryCount, outbox.getId());
                outbox.markAsDlq();
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

    @Scheduled(fixedDelay = 60000)
    public void recoverDlqMessages() {
        List<NotificationOutbox> dlqMessages = outboxRepository.findTop50BySentFalseAndDlqTrueOrderByCreatedAtAsc();

        for (NotificationOutbox outbox : dlqMessages) {
            try {
                NotificationMessage kafkaMessage = new NotificationMessage(
                        outbox.getUserId(),
                        outbox.getEventType(),
                        outbox.getMessage()
                );

                notificationProducer.sendNotification(kafkaMessage);
                outbox.markAsSent(); // sent = true, dlq = false 설정

                log.info("🔄 DLQ 복구 성공 - outboxId: {}, userId: {}", outbox.getId(), outbox.getUserId());

            } catch (Exception e) {
                log.error("❌ DLQ 복구 실패 - outboxId: {}, userId: {}", outbox.getId(), outbox.getUserId(), e);
                // 실패 시 상태 그대로 둠 (다음 스케줄러에서 재시도)
            }
        }

        outboxRepository.saveAll(dlqMessages);
    }
}
