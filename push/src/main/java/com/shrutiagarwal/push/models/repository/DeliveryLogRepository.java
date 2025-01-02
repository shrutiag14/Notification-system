package com.shrutiagarwal.push.models.repository;

import com.shrutiagarwal.push.enums.Channel;
import com.shrutiagarwal.push.enums.Status;
import com.shrutiagarwal.push.models.entity.DeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, Long> {
    List<DeliveryLog> findByNotificationId(Long notificationId);

    List<DeliveryLog> findByChannel(Channel channel);

    List<DeliveryLog> findByStatus(Status status);
}