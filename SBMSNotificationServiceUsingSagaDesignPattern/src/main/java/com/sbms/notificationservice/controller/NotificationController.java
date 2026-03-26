package com.sbms.notificationservice.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbms.notificationservice.dto.BookingDto;
import com.sbms.notificationservice.dto.NotificationDto;
import com.sbms.notificationservice.dto.ResponseMessagedto;
import com.sbms.notificationservice.entity.Notification;
import com.sbms.notificationservice.service.NotificationService;
import com.sbms.notificationservice.utility.NotificationConstants;

@RestController
@RequestMapping("/notify")
public class NotificationController {

	@Autowired
	private NotificationService notificationService;

	@PostMapping("/sendemail")
	public ResponseEntity<ResponseMessagedto<NotificationDto>> sendMail(@RequestBody BookingDto booking) {

		Notification notification = notificationService.sendEmail(booking);
		
		NotificationDto notificationDto = new NotificationDto();
		BeanUtils.copyProperties(notification, notificationDto);

		return ResponseEntity
				.ok(ResponseMessagedto.<NotificationDto>builder().statuscode(HttpStatus.OK.value()).status(NotificationConstants.SUCCESS)
						.message("Mail sent successfully to " + booking.getEmail()).data(notificationDto).build());
	}

	@PostMapping("/sendsms")
	public ResponseEntity<ResponseMessagedto<NotificationDto>> sendSms(@RequestBody BookingDto booking) {

		Notification notification = notificationService.sendSms(booking);
		
		NotificationDto notificationDto = new NotificationDto();
		BeanUtils.copyProperties(notification, notificationDto);

		return ResponseEntity
				.ok(ResponseMessagedto.<NotificationDto>builder().statuscode(HttpStatus.OK.value()).status(NotificationConstants.SUCCESS)
						.message("Sms sent successfully to " + booking.getEmail()).data(notificationDto).build());
	}
}
