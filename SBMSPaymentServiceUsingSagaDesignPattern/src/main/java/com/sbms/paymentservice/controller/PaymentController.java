package com.sbms.paymentservice.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.sbms.paymentservice.dto.*;
import com.sbms.paymentservice.entity.Payment;
import com.sbms.paymentservice.service.PaymentService;
import com.sbms.paymentservice.utility.PaymentStatus;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<ResponseMessagedto<PaymentDto>> processPayment(@RequestBody BookingDto booking) {

        if (booking.getBookingId() == null || booking.getAmount() <= 0) {
            return ResponseEntity.badRequest()
                    .body(ResponseMessagedto.<PaymentDto>builder()
                            .statuscode(HttpStatus.BAD_REQUEST.value())
                            .status(PaymentStatus.FAILED.name())
                            .message("Invalid booking details")
                            .data(null)
                            .build());
        }

        Payment payment = paymentService.processPayment(booking);

        PaymentDto dto = new PaymentDto();
        BeanUtils.copyProperties(payment, dto);

        if (PaymentStatus.FAILED.name().equalsIgnoreCase(payment.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ResponseMessagedto.<PaymentDto>builder()
                            .statuscode(HttpStatus.CONFLICT.value())
                            .status(PaymentStatus.FAILED.name())
                            .message("Payment failed for booking ID: " + booking.getBookingId())
                            .data(dto)
                            .build());
        }

        return ResponseEntity.ok(
                ResponseMessagedto.<PaymentDto>builder()
                        .statuscode(HttpStatus.OK.value())
                        .status(PaymentStatus.SUCCESS.name())
                        .message("Payment processed successfully")
                        .data(dto)
                        .build()
        );
    }

    @PostMapping("/refund")
    public ResponseEntity<ResponseMessagedto<PaymentDto>> refundPayment(@RequestBody BookingDto booking) {

        if (booking.getBookingId() == null) {
            return ResponseEntity.badRequest()
                    .body(ResponseMessagedto.<PaymentDto>builder()
                            .statuscode(HttpStatus.BAD_REQUEST.value())
                            .status(PaymentStatus.FAILED.name())
                            .message("Booking ID is required for refund")
                            .data(null)
                            .build());
        }

        Payment payment = paymentService.refundPayment(booking);

        PaymentDto dto = new PaymentDto();
        BeanUtils.copyProperties(payment, dto);

        return ResponseEntity.ok(
                ResponseMessagedto.<PaymentDto>builder()
                        .statuscode(HttpStatus.OK.value())
                        .status(PaymentStatus.REFUND_INITIATED.name())
                        .message("Refund initiated successfully")
                        .data(dto)
                        .build()
        );
    }
}