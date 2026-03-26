package com.sbms.busbookingsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private Long bookingId;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @Positive(message = "Amount must be greater than 0")
    private double amount;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;

    private String status;     // PENDING, BOOKED, FAILED, CANCELLED

    private Long paymentId;
}
