package com.sbms.orchestration.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMessagedto<T> {
    private Integer statuscode;
    private String status;
    private String message;
    private T data;
}
