package com.sbms.busbookingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "BUS_BOOKING")
public class BusBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(nullable = false, length = 50)
    private String customerName;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, length = 80)
    private String email;

    @Column(name = "PAYMENT_ID")
    private Long paymentId;
}
