package com.sbms.orchestration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbms.orchestration.dto.*;
import com.sbms.orchestration.utility.Constants;

@Service
public class SagaOrchestrationService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // Booking Service
    private static final String BOOKING_CREATE_URL         = "http://BOOKING-SERVICE/booking/bookingdatasave";
    private static final String BOOKING_CANCEL_URL         = "http://BOOKING-SERVICE/booking/cancelbusbooking";
    private static final String BOOKING_UPDATE_PAYMENT_URL = "http://BOOKING-SERVICE/booking/updatepayment";

    // Payment Service
    private static final String PAYMENT_PROCESS_URL        = "http://PAYMENT-SERVICE/payment/process";
    private static final String PAYMENT_REFUND_URL         = "http://PAYMENT-SERVICE/payment/refund";

    // Notification Service
    private static final String NOTIFICATION_EMAIL_URL     = "http://NOTIFICATION-SERVICE/notify/sendemail";
    private static final String NOTIFICATION_SMS_URL       = "http://NOTIFICATION-SERVICE/notify/sendsms";

    public ResponseEntity<ResponseMessagedto<Object>> startSaga(BookingDto requestDto) {

        BookingDto createdBooking = null;
        PaymentDto paymentDto    = null;
        boolean paymentSuccess   = false;

        try {

            /* =======================
               STEP 1: CREATE BOOKING
               ======================= */
            HttpEntity<BookingDto> bookingRequest = buildRequest(requestDto);

            ResponseEntity<ResponseMessagedto<BookingDto>> bookingResponse =
                    restTemplate.exchange(
                            BOOKING_CREATE_URL,
                            HttpMethod.POST,
                            bookingRequest,
                            new ParameterizedTypeReference<ResponseMessagedto<BookingDto>>() {}
                    );

            ResponseMessagedto<BookingDto> bookingBody = bookingResponse.getBody();

            if (bookingBody == null || bookingBody.getData() == null) {
                throw new RuntimeException("Booking creation failed — empty response");
            }

            createdBooking = bookingBody.getData();

            System.out.println("=== STEP 1 COMPLETE ===");
            System.out.println("Booking ID     : " + createdBooking.getBookingId());
            System.out.println("Booking Status : " + createdBooking.getStatus());

            /* =======================
               STEP 2: PROCESS PAYMENT
               ✅ KEY FIX: Payment service returns 409 when payment FAILS.
               RestTemplate throws HttpClientErrorException on non-2xx responses,
               which was causing the saga to jump to the outer catch — skipping
               Steps 3 and 4 entirely (no payment_id saved, no notification sent).
               We now catch HttpClientErrorException.Conflict (409) here, extract
               the payment data from the error body, and continue the saga normally.
               ======================= */
            String paymentStatus = null;

            try {
                HttpEntity<BookingDto> paymentRequest = buildRequest(createdBooking);

                ResponseEntity<ResponseMessagedto<PaymentDto>> paymentResponse =
                        restTemplate.exchange(
                                PAYMENT_PROCESS_URL,
                                HttpMethod.POST,
                                paymentRequest,
                                new ParameterizedTypeReference<ResponseMessagedto<PaymentDto>>() {}
                        );

                ResponseMessagedto<PaymentDto> paymentBody = paymentResponse.getBody();

                if (paymentBody == null) {
                    throw new RuntimeException("Payment response is null");
                }

                paymentDto    = paymentBody.getData();
                paymentStatus = paymentBody.getStatus();

            } catch (HttpClientErrorException.Conflict ex) {

                // Payment service returned 409 — payment FAILED
                // Extract paymentId from the error response body
                System.out.println("=== STEP 2 — Payment returned 409 (FAILED) ===");
                System.out.println("Raw error body: " + ex.getResponseBodyAsString());

                try {
                    ResponseMessagedto<PaymentDto> errorBody = objectMapper.readValue(
                            ex.getResponseBodyAsString(),
                            objectMapper.getTypeFactory().constructParametricType(
                                    ResponseMessagedto.class, PaymentDto.class));

                    paymentDto    = errorBody.getData();
                    paymentStatus = "FAILED";

                    System.out.println("Extracted paymentId from error body: "
                            + (paymentDto != null ? paymentDto.getPaymentId() : "null"));

                } catch (Exception parseEx) {
                    System.out.println("Could not parse 409 error body: " + parseEx.getMessage());
                    paymentDto    = new PaymentDto();
                    paymentStatus = "FAILED";
                }
            }

            System.out.println("=== STEP 2 COMPLETE ===");
            System.out.println("Payment ID     : " + (paymentDto != null ? paymentDto.getPaymentId() : "null"));
            System.out.println("Payment Status : " + paymentStatus);

            // ✅ Set paymentSuccess immediately after getting status
            paymentSuccess = "SUCCESS".equalsIgnoreCase(paymentStatus);

            /* =======================
               DETERMINE BOOKING STATUS
               ======================= */
            String bookingStatus;

            switch (paymentStatus.toUpperCase()) {
                case "SUCCESS" : bookingStatus = "BOOKED";  break;
                case "FAILED"  : bookingStatus = "FAILED";  break;
                case "PENDING" : bookingStatus = "PENDING"; break;
                default        : bookingStatus = "UNKNOWN";
            }

            /* =======================
               STEP 3: UPDATE BOOKING WITH PAYMENT ID
               Now always runs — even when payment FAILED —
               because we caught the 409 above instead of crashing
               ======================= */
            BookingDto updateDto = new BookingDto();
            updateDto.setBookingId(createdBooking.getBookingId());
            updateDto.setPaymentId(paymentDto != null ? paymentDto.getPaymentId() : null);
            updateDto.setStatus(bookingStatus);

            System.out.println("=== STEP 3 DEBUG ===");
            System.out.println("updateDto bookingId : " + updateDto.getBookingId());
            System.out.println("updateDto paymentId : " + updateDto.getPaymentId());
            System.out.println("updateDto status    : " + updateDto.getStatus());

            HttpEntity<BookingDto> updateRequest = buildRequest(updateDto);

            restTemplate.exchange(
                    BOOKING_UPDATE_PAYMENT_URL,
                    HttpMethod.POST,
                    updateRequest,
                    new ParameterizedTypeReference<ResponseMessagedto<BookingDto>>() {}
            );

            System.out.println("=== STEP 3 COMPLETE — updatePayment called ===");

            // ✅ Sync in-memory object so notifications carry
            // the correct paymentId and status
            createdBooking.setPaymentId(paymentDto != null ? paymentDto.getPaymentId() : null);
            createdBooking.setStatus(bookingStatus);

            /* =======================
               STEP 4: SEND NOTIFICATION FOR BOTH SUCCESS AND FAILED
               Now always runs — email/SMS fire for both outcomes
               ======================= */
            try {
                HttpEntity<BookingDto> emailRequest = buildRequest(createdBooking);
                HttpEntity<BookingDto> smsRequest   = buildRequest(createdBooking);

                restTemplate.exchange(
                        NOTIFICATION_EMAIL_URL,
                        HttpMethod.POST,
                        emailRequest,
                        new ParameterizedTypeReference<Object>() {}
                );

                restTemplate.exchange(
                        NOTIFICATION_SMS_URL,
                        HttpMethod.POST,
                        smsRequest,
                        new ParameterizedTypeReference<Object>() {}
                );

                System.out.println("=== STEP 4 COMPLETE — notifications sent ===");

            } catch (Exception notificationEx) {

                System.out.println("=== STEP 4 FAILED — notification error: "
                        + notificationEx.getMessage() + " ===");

                // Compensation ONLY if payment succeeded
                if (paymentSuccess) {
                    System.out.println("Payment was SUCCESS — triggering refund + cancel");

                    restTemplate.exchange(
                            PAYMENT_REFUND_URL,
                            HttpMethod.POST,
                            buildRequest(createdBooking),
                            new ParameterizedTypeReference<Object>() {}
                    );

                    BookingDto cancelDto = new BookingDto();
                    cancelDto.setBookingId(createdBooking.getBookingId());

                    restTemplate.exchange(
                            BOOKING_CANCEL_URL,
                            HttpMethod.POST,
                            buildRequest(cancelDto),
                            new ParameterizedTypeReference<Object>() {}
                    );
                }

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ResponseMessagedto.builder()
                                .statuscode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .status(Constants.FAILED)
                                .message("Notification failed, compensation triggered")
                                .data(createdBooking)
                                .build());
            }

            /* =======================
               STEP 5: HANDLE FAILED PAYMENT
               cancelBooking() runs here — AFTER Step 3 has already
               committed payment_id to DB
               ======================= */
            System.out.println("=== STEP 5 — handling status: " + paymentStatus + " ===");

            if ("FAILED".equalsIgnoreCase(paymentStatus)) {

                BookingDto cancelDto = new BookingDto();
                cancelDto.setBookingId(createdBooking.getBookingId());

                restTemplate.exchange(
                        BOOKING_CANCEL_URL,
                        HttpMethod.POST,
                        buildRequest(cancelDto),
                        new ParameterizedTypeReference<Object>() {}
                );

                System.out.println("=== Booking cancelled due to FAILED payment ===");

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ResponseMessagedto.builder()
                                .statuscode(HttpStatus.CONFLICT.value())
                                .status(Constants.FAILED)
                                .message("Payment failed, booking cancelled")
                                .data(createdBooking)
                                .build());
            }

            if ("PENDING".equalsIgnoreCase(paymentStatus)) {

                return ResponseEntity.ok(
                        ResponseMessagedto.builder()
                                .statuscode(HttpStatus.ACCEPTED.value())
                                .status("PENDING")
                                .message("Payment is pending")
                                .data(createdBooking)
                                .build()
                );
            }

            /* =======================
               SUCCESS RESPONSE
               ======================= */
            return ResponseEntity.ok(
                    ResponseMessagedto.builder()
                            .statuscode(HttpStatus.OK.value())
                            .status(Constants.SUCCESS)
                            .message("Saga completed successfully")
                            .data(createdBooking)
                            .build()
            );

        } catch (Exception ex) {

            System.out.println("=== SAGA FAILED with exception: " + ex.getMessage() + " ===");

            try {
                if (paymentSuccess && createdBooking != null) {
                    System.out.println("Refunding payment for booking: "
                            + createdBooking.getBookingId());

                    restTemplate.exchange(
                            PAYMENT_REFUND_URL,
                            HttpMethod.POST,
                            buildRequest(createdBooking),
                            new ParameterizedTypeReference<Object>() {}
                    );
                }

                if (createdBooking != null) {
                    System.out.println("Cancelling booking: " + createdBooking.getBookingId());

                    BookingDto cancelDto = new BookingDto();
                    cancelDto.setBookingId(createdBooking.getBookingId());

                    restTemplate.exchange(
                            BOOKING_CANCEL_URL,
                            HttpMethod.POST,
                            buildRequest(cancelDto),
                            new ParameterizedTypeReference<Object>() {}
                    );
                }

            } catch (Exception ignore) {
                System.out.println("Compensation also failed: " + ignore.getMessage());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseMessagedto.builder()
                            .statuscode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(Constants.FAILED)
                            .message("Saga failed: " + ex.getMessage())
                            .data(null)
                            .build());
        }
    }

    // ✅ Helper: builds HttpEntity with JSON headers for any request body
    private <T> HttpEntity<T> buildRequest(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}