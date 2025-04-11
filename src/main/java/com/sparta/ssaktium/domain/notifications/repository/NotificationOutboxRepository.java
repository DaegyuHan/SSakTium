package com.sparta.ssaktium.domain.notifications.repository;

import com.sparta.ssaktium.domain.notifications.entity.NotificationOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {
    List<NotificationOutbox> findTop50BySentFalseOrderByCreatedAtAsc();

    List<NotificationOutbox> findBySentFalse();
}
