package com.sbms.busbookingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbms.busbookingsystem.entity.BusBooking;
import com.sbms.busbookingsystem.exception.ResourceNotFoundException;
import com.sbms.busbookingsystem.repo.BusBookingRepo;
import com.sbms.busbookingsystem.utility.BookingStatus;

@Service
@Transactional
public class BusBookingServiceImpl implements BusBookingService {

    @Autowired
    private BusBookingRepo bookingRepo;

    // ✅ CREATE BOOKING
    @Override
    public BusBooking createBooking(BusBooking booking) {
        booking.setStatus(BookingStatus.PENDING.name());
        BusBooking saved = bookingRepo.save(booking);

        System.out.println("Booking created with ID: " + saved.getBookingId());

        return saved;
    }

    // ✅ CANCEL BOOKING (DO NOT TOUCH PAYMENT ID)
    @Override
    public BusBooking cancelBooking(Long bookingId) {

        BusBooking existing = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with id: " + bookingId));

        if (!BookingStatus.CANCELLED.name().equalsIgnoreCase(existing.getStatus())) {

            existing.setStatus(BookingStatus.CANCELLED.name());

            // ❗ IMPORTANT: DO NOT modify paymentId
            BusBooking updated = bookingRepo.save(existing);

            System.out.println("Booking cancelled: " + updated.getBookingId() +
                    " | PaymentId: " + updated.getPaymentId());

            return updated;
        }

        return existing;
    }

    // ✅ UPDATE PAYMENT (SAFE + RELIABLE)
    @Override
    public BusBooking updatePayment(Long bookingId, Long paymentId, String status) {

        BusBooking book = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with id: " + bookingId));

        // 🔥 ONLY update paymentId if NOT NULL
        if (paymentId != null) {
            book.setPaymentId(paymentId);
        }

        // 🔥 Always update status if provided
        if (status != null && !status.isBlank()) {
            book.setStatus(status);
        }

        BusBooking updated = bookingRepo.save(book);

        System.out.println("Booking updated:");
        System.out.println("ID: " + updated.getBookingId());
        System.out.println("PaymentId: " + updated.getPaymentId());
        System.out.println("Status: " + updated.getStatus());

        return updated;
    }
}