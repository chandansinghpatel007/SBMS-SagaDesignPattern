package com.sbms.orchestration.feignClients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.sbms.orchestration.dto.*;

@FeignClient(name = "PAYMENT-SERVICE")
public interface PaymentFeignClient {

    @PostMapping("/payment/process")
    ResponseMessagedto<PaymentDto> processPayment(@RequestBody BookingDto booking);

    @PostMapping("/payment/refund")
    ResponseMessagedto<PaymentDto> refundPayment(@RequestBody BookingDto booking);
}
