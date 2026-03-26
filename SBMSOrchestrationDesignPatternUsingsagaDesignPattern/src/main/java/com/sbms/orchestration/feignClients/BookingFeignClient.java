package com.sbms.orchestration.feignClients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.sbms.orchestration.dto.BookingDto;
import com.sbms.orchestration.dto.ResponseMessagedto;

@FeignClient(name = "BOOKING-SERVICE")
public interface BookingFeignClient {

    @PostMapping("/booking/bookingdatasave")
    ResponseMessagedto<BookingDto> createBooking(@RequestBody BookingDto booking);

    @PostMapping("/booking/cancelbusbooking")
    ResponseMessagedto<BookingDto> cancelBooking(@RequestBody BookingDto booking);

    @PostMapping("/booking/updatepayment")
    ResponseMessagedto<BookingDto> updatePayment(@RequestBody BookingDto dto);
}