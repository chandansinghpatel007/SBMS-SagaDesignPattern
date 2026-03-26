package com.sbms.paymentservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "PAYMENT_INFO")
public class Payment {

	@Id
	@SequenceGenerator(name = "payment_seq_gen", sequenceName = "PAYMENT_SEQ", allocationSize = 1,initialValue = 1001)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_seq_gen")
	private Long paymentId;

	@Column(nullable = false)
	private Long bookingId;

	@Column(nullable = false)
	private double amount;

	@Column(nullable = false, length = 30)
	private String status;
}
