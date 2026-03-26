package com.sbms.notificationservice.service;

import com.sbms.notificationservice.dto.BookingDto;
import com.sbms.notificationservice.entity.Notification;

public interface NotificationService {

    Notification sendEmail(BookingDto booking);

    Notification sendSms(BookingDto booking);
}
