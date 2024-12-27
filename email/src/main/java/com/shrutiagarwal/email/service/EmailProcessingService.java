package com.shrutiagarwal.email.service;

import com.shrutiagarwal.email.enums.Channel;
import com.shrutiagarwal.email.enums.Status;
import com.shrutiagarwal.email.exceptions.NotificationNotFoundException;
import com.shrutiagarwal.email.models.entity.DeliveryLog;
import com.shrutiagarwal.email.models.entity.Notification;
import com.shrutiagarwal.email.models.repository.DeliveryLogRepository;
import com.shrutiagarwal.email.models.repository.NotificationRepository;
import com.shrutiagarwal.email.request.EmailRequest;
import com.shrutiagarwal.email.response.EmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailProcessingService {
    private final EmailService emailService;
    private final DeliveryLogRepository deliveryLogRepository;
    private final NotificationRepository notificationRepository;
    private final FailedNotificationsHandlerService failedNotificationsHandlerService;

    @Autowired
    public EmailProcessingService(EmailService emailService,
                                  DeliveryLogRepository deliveryLogRepository,
                                  NotificationRepository notificationRepository,
                                  FailedNotificationsHandlerService failedNotificationsHandlerService) {
        this.emailService = emailService;
        this.deliveryLogRepository = deliveryLogRepository;
        this.notificationRepository = notificationRepository;
        this.failedNotificationsHandlerService = failedNotificationsHandlerService;
    }

    public void processEmail(EmailRequest emailRequest) {
        EmailResponse response = sendEmailToVendors(emailRequest);

        if (response.getStatus() >= 200 && response.getStatus() < 300) {
            updateNotificationStatus(emailRequest);
            createDeliveryLog(emailRequest);
        } else {
            failedNotificationsHandlerService.handleFailedRequest(emailRequest);
        }
    }

    private void updateNotificationStatus(EmailRequest emailRequest) {
        try {
            Notification notification = notificationRepository.findById(emailRequest.getNotificationId())
                    .orElseThrow(() -> new NotificationNotFoundException("Notification with Id: " + emailRequest.getNotificationId() + " Not found"));
            notification.setStatus(Status.sent);
            notificationRepository.save(notification);
            log.info("Status updated to SENT for Notification Id: {}", emailRequest.getNotificationId());
        } catch (NotificationNotFoundException ex) {
            log.error("Notification with Id: {} not found while updating status", emailRequest.getNotificationId());
        } catch (Exception ex) {
            log.error("Exception while updating status of Notification for emailRequest: {}", emailRequest);
            log.error("Exception: {}", ex.toString());
        }
    }

    private void createDeliveryLog(EmailRequest emailRequest) {
        try {
            Notification notification = notificationRepository.findById(emailRequest.getNotificationId())
                    .orElseThrow(() -> new NotificationNotFoundException("Notification with Id: " + emailRequest.getNotificationId() + " Not found"));
            deliveryLogRepository.save(new DeliveryLog(notification, Channel.EMAIL, Status.sent, ""));
        } catch (Exception ex) {
            log.error("Exception while creating delivery log for Notification id {} for emailRequest: {}", emailRequest.getNotificationId(), emailRequest);
            log.error("Exception: {}", ex.toString());
        }
    }

    private EmailResponse sendEmailToVendors(EmailRequest emailRequest) {
        EmailResponse sendEmailResponse =  emailService.sendEmail(emailRequest);

        if(sendEmailResponse.getStatus() >= 200 && sendEmailResponse.getStatus() <300){
            sendEmailResponse.setMessage("Email Sent");
            log.info("EmailRequest {} sent successfully",emailRequest);
        } else {
            log.error("Failed to send EmailRequest {}. Message: {}",emailRequest, sendEmailResponse.getMessage());
            sendEmailResponse.setMessage("Something went wrong with SendGrid.");
        }
        return sendEmailResponse;
    }
}