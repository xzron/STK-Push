package com.hezron.stkpush.repository;

import com.hezron.stkpush.entity.MpesaTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MpesaTransactionsRepository extends JpaRepository<MpesaTransaction, Long> {

    Optional<MpesaTransaction> findByCheckoutRequestID(String checkoutRequestID);
}
