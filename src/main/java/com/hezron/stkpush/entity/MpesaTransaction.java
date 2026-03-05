package com.hezron.stkpush.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Data
@Table(name = "mpesa_transactions")
public class MpesaTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String phoneNumber;
    private Double amount;
    private String checkoutRequestID;
    private String merchantRequestID;
    private String resultCode;
    private String resultDesc;
    private String status;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }
}
