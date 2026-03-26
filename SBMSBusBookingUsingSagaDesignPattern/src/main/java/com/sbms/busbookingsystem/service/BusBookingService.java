package com.sbms.busbookingsystem.service;

import com.sbms.busbookingsystem.entity.BusBooking;

public interface BusBookingService {

    BusBooking createBooking(BusBooking booking);

    BusBooking cancelBooking(Long bookingId);

    BusBooking updatePayment(Long bookingId, Long paymentId, String status);
}
