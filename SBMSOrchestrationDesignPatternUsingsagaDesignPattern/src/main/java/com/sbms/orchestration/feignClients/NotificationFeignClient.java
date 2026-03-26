package com.sbms.orchestration.feignClients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.sbms.orchestration.dto.*;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationFeignClient {

    @PostMapping("/notify/sendemail")
    ResponseMessagedto<NotificationDto> sendEmail(@RequestBody BookingDto booking);

    @PostMapping("/notify/sendsms")
    ResponseMessagedto<NotificationDto> sendSms(@RequestBody BookingDto booking);
}
