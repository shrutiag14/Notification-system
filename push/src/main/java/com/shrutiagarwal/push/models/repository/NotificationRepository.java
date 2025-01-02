package com.shrutiagarwal.push.models.repository;

import com.shrutiagarwal.push.enums.Channel;
import com.shrutiagarwal.push.enums.Status;
import com.shrutiagarwal.push.models.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);

    List<Notification> findByUserIdAndChannel(Long userId, Channel channel);

    List<Notification> findByStatus(Status status);
}