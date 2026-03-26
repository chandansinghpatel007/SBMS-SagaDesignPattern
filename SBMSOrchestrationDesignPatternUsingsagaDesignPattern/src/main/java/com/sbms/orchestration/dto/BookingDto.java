package com.sbms.orchestration.dto;

import lombok.Data;

@Data
public class BookingDto {
    private Long bookingId;
    private String customerName;
    private String email;
    private double amount;
    private String status;
    private Long paymentId;
}
