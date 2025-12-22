package com.mindwell.be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "vnpay_transactions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vnpay_transactions_payment_id", columnNames = {"payment_id"}),
                @UniqueConstraint(name = "uk_vnpay_transactions_vnp_txn_ref", columnNames = {"vnp_txn_ref"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VnpayTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "vnp_txn_ref")
    private String vnpTxnRef;

    @Column(name = "vnp_transaction_no")
    private String vnpTransactionNo;

    @Column(name = "vnp_response_code")
    private String vnpResponseCode;

    @Column(name = "vnp_transaction_status")
    private String vnpTransactionStatus;

    @Column(name = "request_params_json", columnDefinition = "text")
    private String requestParamsJson;

    @Column(name = "request_hash_data", columnDefinition = "text")
    private String requestHashData;

    @Column(name = "request_secure_hash")
    private String requestSecureHash;

    @Column(name = "request_url", columnDefinition = "text")
    private String requestUrl;

    @Column(name = "return_params_json", columnDefinition = "text")
    private String returnParamsJson;

    @Column(name = "return_query", columnDefinition = "text")
    private String returnQuery;

    @Column(name = "return_secure_hash")
    private String returnSecureHash;

    @Column(name = "return_verified")
    private Boolean returnVerified;

    @Column(name = "return_received_at")
    private LocalDateTime returnReceivedAt;

    @Column(name = "ipn_params_json", columnDefinition = "text")
    private String ipnParamsJson;

    @Column(name = "ipn_query", columnDefinition = "text")
    private String ipnQuery;

    @Column(name = "ipn_secure_hash")
    private String ipnSecureHash;

    @Column(name = "ipn_verified")
    private Boolean ipnVerified;

    @Column(name = "ipn_received_at")
    private LocalDateTime ipnReceivedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
