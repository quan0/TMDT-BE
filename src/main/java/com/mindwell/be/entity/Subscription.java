package com.mindwell.be.entity;

import com.mindwell.be.entity.enums.SubscriptionBillingCycle;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer subId;

    private String name;
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private SubscriptionBillingCycle billingCycle;

    private String tierSubtitle;
    private String badgeLabel;
    private String shortDesc;

    @Column(columnDefinition = "text")
    private String features;

    private Boolean isActive;
}
