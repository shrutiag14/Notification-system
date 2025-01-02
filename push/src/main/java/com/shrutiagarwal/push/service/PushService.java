package com.shrutiagarwal.push.service;

import com.shrutiagarwal.push.request.PushRequest;
import com.shrutiagarwal.push.response.PushResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PushService {

    public PushResponse sendPushNotification(PushRequest pushNRequest){
        return new PushResponse(202,"Push N sent successfully");
    }
}