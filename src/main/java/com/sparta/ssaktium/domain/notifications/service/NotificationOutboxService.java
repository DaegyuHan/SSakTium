package com.sparta.ssaktium.domain.notifications.service;

import com.sparta.ssaktium.domain.notifications.dto.EventType;
import com.sparta.ssaktium.domain.notifications.entity.NotificationOutbox;
import com.sparta.ssaktium.domain.notifications.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationOutboxService {

    private final NotificationOutboxRepository outboxRepository;

    @Transactional
    public void saveOutbox(Long userId, EventType eventType, String message) {
        NotificationOutbox outbox = new NotificationOutbox(userId, eventType, message);
        outboxRepository.save(outbox);
    }
}
