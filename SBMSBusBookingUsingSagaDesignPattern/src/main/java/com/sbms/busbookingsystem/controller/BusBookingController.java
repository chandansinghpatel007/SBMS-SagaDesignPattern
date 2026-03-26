package com.sbms.busbookingsystem.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.sbms.busbookingsystem.dto.*;
import com.sbms.busbookingsystem.entity.BusBooking;
import com.sbms.busbookingsystem.service.BusBookingService;
import com.sbms.busbookingsystem.utility.Constants;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/booking")
public class BusBookingController {

	@Autowired
	private BusBookingService bookingService;

	// ✅ CREATE BOOKING
	@PostMapping("/bookingdatasave")
	public ResponseEntity<ResponseMessagedto<BookingDto>> createBooking(@Valid @RequestBody BookingDto request) {

		BusBooking booking = new BusBooking();
		booking.setCustomerName(request.getCustomerName());
		booking.setAmount(request.getAmount());
		booking.setEmail(request.getEmail());

		BusBooking saved = bookingService.createBooking(booking);

		BookingDto responseDto = new BookingDto();
		BeanUtils.copyProperties(saved, responseDto);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseMessagedto.<BookingDto>builder().statuscode(HttpStatus.CREATED.value())
						.status(Constants.SUCCESS).message("Booking created successfully").data(responseDto).build());
	}

	// ✅ CANCEL BOOKING
	@PostMapping("/cancelbusbooking")
	public ResponseEntity<ResponseMessagedto<BookingDto>> cancelBooking(@RequestBody BookingDto booking) {

		if (booking.getBookingId() == null) {
			return ResponseEntity.badRequest()
					.body(ResponseMessagedto.<BookingDto>builder().statuscode(HttpStatus.BAD_REQUEST.value())
							.status(Constants.FAILURE).message("Booking ID is required for cancellation").data(null)
							.build());
		}

		BusBooking cancelled = bookingService.cancelBooking(booking.getBookingId());

		BookingDto responseDto = new BookingDto();
		BeanUtils.copyProperties(cancelled, responseDto);

		return ResponseEntity.ok(ResponseMessagedto.<BookingDto>builder().statuscode(HttpStatus.OK.value())
				.status(Constants.SUCCESS).message("Booking cancelled successfully").data(responseDto).build());
	}

	// ✅ UPDATE PAYMENT
	@PostMapping("/updatepayment")
	public ResponseEntity<ResponseMessagedto<BookingDto>> updatePayment(@RequestBody BookingDto request) {

		if (request.getBookingId() == null || request.getPaymentId() == null) {
			return ResponseEntity.badRequest()
					.body(ResponseMessagedto.<BookingDto>builder().statuscode(HttpStatus.BAD_REQUEST.value())
							.status(Constants.FAILURE).message("bookingId and paymentId are required").data(null)
							.build());
		}

		BusBooking updated = bookingService.updatePayment(request.getBookingId(), request.getPaymentId(),
				request.getStatus());

		BookingDto responseDto = new BookingDto();
		BeanUtils.copyProperties(updated, responseDto);

		return ResponseEntity.ok(ResponseMessagedto.<BookingDto>builder().statuscode(HttpStatus.OK.value())
				.status(Constants.SUCCESS).message("PaymentId updated successfully").data(responseDto).build());
	}
}