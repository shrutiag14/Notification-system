package com.shrutiagarwal.push.service;

import com.shrutiagarwal.push.request.PushRequest;
import org.springframework.stereotype.Service;

@Service
public class FailedNotificationsHandlerService {
    public void handleFailedRequest(PushRequest pushRequest){
        //implement retry strategy or logging for failed notifications
    }
}