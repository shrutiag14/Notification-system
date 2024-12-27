package com.shrutiagarwal.email.service;

import com.shrutiagarwal.email.request.EmailRequest;
import org.springframework.stereotype.Service;

@Service
public class FailedNotificationsHandlerService {
    public void handleFailedRequest(EmailRequest emailRequest){
        //implement retry strategy or logging for failed notifications
    }
}