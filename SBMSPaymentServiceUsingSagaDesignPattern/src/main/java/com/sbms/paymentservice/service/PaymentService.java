package com.sbms.paymentservice.service;

import com.sbms.paymentservice.dto.BookingDto;
import com.sbms.paymentservice.entity.Payment;

public interface PaymentService {

    Payment processPayment(BookingDto booking);

    Payment refundPayment(BookingDto booking);
}
