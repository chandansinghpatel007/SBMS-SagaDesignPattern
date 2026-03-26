package com.sbms.notificationservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "Notifications")
public class Notification {

	@Id
	private String notificationId;
	private String email;
	private String message;
	private String type; // EMAIL / SMS
	private String status; // SENT / FAILED
}
