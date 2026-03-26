package com.sbms.notificationservice.dto;

import lombok.Data;

@Data
public class NotificationDto {

	private String notificationId;
    private String email;
    private String message;
    private String type;   // EMAIL / SMS
    private String status; // SENT / FAILED
}
