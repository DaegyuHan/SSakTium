package com.sparta.ssaktium.domain.notifications.entity;

import com.sparta.ssaktium.domain.notifications.dto.EventType;
import com.sparta.ssaktium.domain.notifications.dto.NotificationMessage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_outbox")
@Getter
@NoArgsConstructor
public class NotificationOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private String message;

    private boolean sent = true;

    private boolean dlq = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public NotificationOutbox(Long userId, EventType eventType, String message) {
        this.userId = userId;
        this.eventType = eventType;
        this.message = message;
    }

    public void markAsSent() {
        this.sent = true;
        this.dlq = false;
    }

    public void markAsUnsent() {
        this.sent = false;
    }

    public void markAsDlq() {
        this.dlq = true;
    }
}
