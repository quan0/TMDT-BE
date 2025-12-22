package com.mindwell.be.repository;

import com.mindwell.be.entity.VnpayTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VnpayTransactionRepository extends JpaRepository<VnpayTransaction, Integer> {
    Optional<VnpayTransaction> findByPaymentPaymentId(Integer paymentId);

    Optional<VnpayTransaction> findByVnpTxnRef(String vnpTxnRef);
}
