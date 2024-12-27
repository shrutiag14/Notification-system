package com.shrutiagarwal.email.models.repository;

import com.shrutiagarwal.email.enums.Channel;
import com.shrutiagarwal.email.enums.Status;
import com.shrutiagarwal.email.models.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);

    List<Notification> findByUserIdAndChannel(Long userId, Channel channel);

    List<Notification> findByStatus(Status status);
}