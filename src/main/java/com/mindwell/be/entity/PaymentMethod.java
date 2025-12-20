package com.mindwell.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "payment_methods",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_methods_method_key", columnNames = {"method_key"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer methodId;

    private String methodKey;
    private String displayName;
    private String badgeLabel;
    private Boolean isActive;
}
