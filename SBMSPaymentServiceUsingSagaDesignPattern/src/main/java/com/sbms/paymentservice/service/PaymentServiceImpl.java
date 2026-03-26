package com.sbms.paymentservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbms.paymentservice.dto.BookingDto;
import com.sbms.paymentservice.entity.Payment;
import com.sbms.paymentservice.exception.ResourceNotFoundException;
import com.sbms.paymentservice.repo.PaymentRepo;
import com.sbms.paymentservice.utility.PaymentConstants;
import com.sbms.paymentservice.utility.PaymentStatus;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepo paymentRepo;

    @Override
    public Payment processPayment(BookingDto booking) {

        Payment payment = new Payment();
        payment.setBookingId(booking.getBookingId());
        payment.setAmount(booking.getAmount());

        if (booking.getAmount() < PaymentConstants.MIN_PAYMENT_AMOUNT) {
            payment.setStatus(PaymentStatus.FAILED.name());
        } else {
            payment.setStatus(PaymentStatus.SUCCESS.name());
        }

        return paymentRepo.save(payment);
    }

    @Override
    public Payment refundPayment(BookingDto booking) {

        Payment payment = paymentRepo.findByBookingId(booking.getBookingId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found for bookingId: " + booking.getBookingId()));

        payment.setStatus(PaymentStatus.REFUND_INITIATED.name());

        return paymentRepo.save(payment);
    }
}
