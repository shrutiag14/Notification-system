package com.shrutiagarwal.push.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushRequest {
    private String title;
    private String message;
    private String action;
    private Long notificationId;

    public PushRequest(String title, String message, String action){
        this.title = title;
        this.message = message;
        this.action = action;
    }
}