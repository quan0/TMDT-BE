package com.mindwell.be.entity;

import com.mindwell.be.entity.enums.MindPointTransactionReason;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mind_point_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MindPointTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Integer pointsAmount;

    @Enumerated(EnumType.STRING)
    private MindPointTransactionReason reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_payment_id")
    private Payment relatedPayment;
}
