package com.shrutiagarwal.email.request;

import lombok.*;

@Data
public class EmailRequest {
    private String emailId;
    private String message;
    private String emailSubject;
    private String[] emailAttachments;
    private Long notificationId;
}