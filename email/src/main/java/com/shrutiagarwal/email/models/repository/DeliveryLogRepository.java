package com.shrutiagarwal.email.models.repository;

import com.shrutiagarwal.email.enums.Channel;
import com.shrutiagarwal.email.enums.Status;
import com.shrutiagarwal.email.models.entity.DeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, Long> {
    List<DeliveryLog> findByNotificationId(Long notificationId);

    List<DeliveryLog> findByChannel(Channel channel);

    List<DeliveryLog> findByStatus(Status status);
}