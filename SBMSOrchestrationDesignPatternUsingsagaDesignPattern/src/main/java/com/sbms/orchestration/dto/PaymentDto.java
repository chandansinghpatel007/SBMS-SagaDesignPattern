package com.sbms.orchestration.dto;

import lombok.Data;

@Data
public class PaymentDto {
    private Long paymentId;
    private Long bookingId;
    private double amount;
    private String status;
}
