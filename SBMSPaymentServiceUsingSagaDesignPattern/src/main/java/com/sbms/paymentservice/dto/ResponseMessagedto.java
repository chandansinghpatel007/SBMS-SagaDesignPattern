package com.sbms.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMessagedto<T> {

    private Integer statuscode;
    private String status;     // SUCCESS / FAILED
    private String message;
    private T data;
}
