package com.hezron.stkpush.dto;


import lombok.Data;

@Data
public class StkPushRequest {
    private String phoneNumber;
    private Double amount;
}
