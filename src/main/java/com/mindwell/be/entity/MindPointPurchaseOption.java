package com.mindwell.be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "mind_point_purchase_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MindPointPurchaseOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer optionId;

    private Integer pointsAmount;
    private BigDecimal priceVnd;

    private String name;
    private String description;
    private String badgeLabel;
    private Boolean isActive;
}
