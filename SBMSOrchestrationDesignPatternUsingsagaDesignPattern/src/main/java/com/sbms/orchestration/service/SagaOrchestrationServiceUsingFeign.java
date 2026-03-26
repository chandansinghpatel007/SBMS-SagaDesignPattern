package com.sbms.orchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import com.sbms.orchestration.dto.*;
import com.sbms.orchestration.feignClients.*;
import com.sbms.orchestration.utility.Constants;

@Service
public class SagaOrchestrationServiceUsingFeign {

    @Autowired
    private BookingFeignClient bookingClient;

    @Autowired
    private PaymentFeignClient paymentClient;

    @Autowired
    private NotificationFeignClient notificationClient;

    @Autowired
    private ObjectMapper objectMapper;

    public ResponseEntity<ResponseMessagedto<Object>> startSaga(BookingDto requestDto) {

        BookingDto createdBooking = null;
        PaymentDto paymentDto = null;
        boolean paymentSuccess = false;

        try {

            /* =======================
               STEP 1: CREATE BOOKING
               ======================= */
            ResponseMessagedto<BookingDto> bookingResponse =
                    bookingClient.createBooking(requestDto);

            createdBooking = bookingResponse.getData();

            System.out.println("=== STEP 1 COMPLETE ===");
            System.out.println("Booking ID: " + createdBooking.getBookingId());
            System.out.println("Booking Status: " + createdBooking.getStatus());

            /* =======================
               STEP 2: PROCESS PAYMENT
               ✅ KEY FIX: Payment service returns 409 when payment FAILS.
               Feign throws FeignException on any non-2xx response, which
               was causing the saga to jump to the outer catch — skipping
               Steps 3 and 4 entirely (no payment_id saved, no notification).
               We now catch FeignException here, extract the payment data
               from the error response body, and continue the saga normally.
               ======================= */
            String paymentStatus = null;

            try {
                ResponseMessagedto<PaymentDto> paymentResponse =
                        paymentClient.processPayment(createdBooking);

                paymentDto = paymentResponse.getData();
                paymentStatus = paymentResponse.getStatus();

            } catch (FeignException.Conflict ex) {
                // Payment service returned 409 — payment FAILED
                // Extract paymentDto from the error response body
                System.out.println("=== STEP 2 — Payment returned 409 (FAILED) ===");
                System.out.println("Raw error body: " + ex.contentUTF8());

                try {
                    // Parse the error body to get paymentId
                    ResponseMessagedto<PaymentDto> errorResponse = objectMapper.readValue(
                            ex.contentUTF8(),
                            objectMapper.getTypeFactory().constructParametricType(
                                    ResponseMessagedto.class, PaymentDto.class));

                    paymentDto = errorResponse.getData();
                    paymentStatus = "FAILED";

                    System.out.println("Extracted paymentId from error body: "
                            + paymentDto.getPaymentId());

                } catch (Exception parseEx) {
                    // If parsing fails, we still know payment failed
                    // but we don't have a paymentId
                    System.out.println("Could not parse error body: " + parseEx.getMessage());
                    paymentDto = new PaymentDto();
                    paymentStatus = "FAILED";
                }
            }

            System.out.println("=== STEP 2 COMPLETE ===");
            System.out.println("Payment ID: " + (paymentDto != null ? paymentDto.getPaymentId() : "null"));
            System.out.println("Payment Status: " + paymentStatus);

            // Set paymentSuccess flag immediately after getting status
            paymentSuccess = "SUCCESS".equalsIgnoreCase(paymentStatus);

            /* =======================
               STEP 3: UPDATE BOOKING WITH PAYMENT ID
               Now always runs — even when payment FAILED —
               because we caught the 409 above instead of crashing
               ======================= */
            BookingDto updateDto = new BookingDto();
            updateDto.setBookingId(createdBooking.getBookingId());
            updateDto.setPaymentId(paymentDto != null ? paymentDto.getPaymentId() : null);
            updateDto.setStatus(paymentStatus);

            System.out.println("=== STEP 3 DEBUG ===");
            System.out.println("updateDto bookingId : " + updateDto.getBookingId());
            System.out.println("updateDto paymentId : " + updateDto.getPaymentId());
            System.out.println("updateDto status    : " + updateDto.getStatus());

            bookingClient.updatePayment(updateDto);

            System.out.println("=== STEP 3 COMPLETE — updatePayment called ===");

            // Sync in-memory object for notifications
            createdBooking.setPaymentId(paymentDto != null ? paymentDto.getPaymentId() : null);
            createdBooking.setStatus(paymentStatus);

            /* =======================
               STEP 4: SEND NOTIFICATION FOR BOTH SUCCESS AND FAILED
               Now always runs — email/SMS fire for both outcomes
               ======================= */
            try {
                notificationClient.sendEmail(createdBooking);
                notificationClient.sendSms(createdBooking);

                System.out.println("=== STEP 4 COMPLETE — notifications sent ===");

            } catch (Exception notificationEx) {

                System.out.println("=== STEP 4 FAILED — notification error: "
                        + notificationEx.getMessage() + " ===");

                if (paymentSuccess) {
                    System.out.println("Payment was SUCCESS — triggering refund + cancel");
                    paymentClient.refundPayment(createdBooking);

                    BookingDto cancelDto = new BookingDto();
                    cancelDto.setBookingId(createdBooking.getBookingId());
                    bookingClient.cancelBooking(cancelDto);
                }

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ResponseMessagedto.builder()
                                .statuscode(500)
                                .status(Constants.FAILED)
                                .message("Notification failed, compensation triggered")
                                .data(createdBooking)
                                .build());
            }

            /* =======================
               STEP 5: HANDLE PAYMENT STATUS
               cancelBooking() runs here — AFTER Step 3 has already
               committed payment_id to DB
               ======================= */
            System.out.println("=== STEP 5 — handling status: " + paymentStatus + " ===");

            if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {

                return ResponseEntity.ok(
                        ResponseMessagedto.builder()
                                .statuscode(200)
                                .status(Constants.SUCCESS)
                                .message("Booking completed successfully")
                                .data(createdBooking)
                                .build()
                );
            }

            if ("FAILED".equalsIgnoreCase(paymentStatus)) {

                BookingDto cancelDto = new BookingDto();
                cancelDto.setBookingId(createdBooking.getBookingId());
                bookingClient.cancelBooking(cancelDto);

                System.out.println("=== Booking cancelled due to FAILED payment ===");

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ResponseMessagedto.builder()
                                .statuscode(409)
                                .status(Constants.FAILED)
                                .message("Payment failed, booking cancelled")
                                .data(createdBooking)
                                .build());
            }

            if ("PENDING".equalsIgnoreCase(paymentStatus)) {

                return ResponseEntity.ok(
                        ResponseMessagedto.builder()
                                .statuscode(202)
                                .status("PENDING")
                                .message("Payment is pending")
                                .data(createdBooking)
                                .build()
                );
            }

            return ResponseEntity.ok(
                    ResponseMessagedto.builder()
                            .statuscode(200)
                            .status(Constants.SUCCESS)
                            .message("Saga completed")
                            .data(createdBooking)
                            .build()
            );

        } catch (Exception ex) {

            System.out.println("=== SAGA FAILED with exception: " + ex.getMessage() + " ===");

            try {
                if (paymentSuccess && createdBooking != null) {
                    System.out.println("Refunding payment for booking: "
                            + createdBooking.getBookingId());
                    paymentClient.refundPayment(createdBooking);
                }

                if (createdBooking != null) {
                    System.out.println("Cancelling booking: " + createdBooking.getBookingId());
                    BookingDto cancelDto = new BookingDto();
                    cancelDto.setBookingId(createdBooking.getBookingId());
                    bookingClient.cancelBooking(cancelDto);
                }

            } catch (Exception ignore) {
                System.out.println("Compensation also failed: " + ignore.getMessage());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseMessagedto.builder()
                            .statuscode(500)
                            .status(Constants.FAILED)
                            .message("Saga failed: " + ex.getMessage())
                            .data(null)
                            .build());
        }
    }
}