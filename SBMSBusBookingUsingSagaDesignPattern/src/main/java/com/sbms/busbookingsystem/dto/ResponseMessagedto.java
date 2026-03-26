package com.sbms.busbookingsystem.dto;

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
    private String status;   // SUCCESS / FAILURE
    private String message;
    private T data;
}
