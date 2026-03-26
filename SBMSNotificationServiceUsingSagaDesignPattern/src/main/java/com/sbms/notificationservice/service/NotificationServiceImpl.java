package com.sbms.notificationservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sbms.notificationservice.dto.BookingDto;
import com.sbms.notificationservice.entity.Notification;
import com.sbms.notificationservice.repository.NotificationRepo;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepo repo;

    @Override
    public Notification sendEmail(BookingDto booking) {

        Notification n = new Notification();
        n.setEmail(booking.getEmail());
        n.setType("EMAIL");
        n.setStatus("SENT");
        n.setMessage("Booking " + booking.getStatus() + " | Amount: " + booking.getAmount());

        System.out.println("✅ Email sent to: " + booking.getEmail());

        return repo.save(n);
    }

    @Override
    public Notification sendSms(BookingDto booking) {

        Notification n = new Notification();
        n.setEmail(booking.getEmail());
        n.setType("SMS");
        n.setStatus("SENT");
        n.setMessage("SMS: Booking " + booking.getStatus() + " | Amount: " + booking.getAmount());

        System.out.println("✅ SMS sent to: " + booking.getEmail());

        return repo.save(n);
    }
}
