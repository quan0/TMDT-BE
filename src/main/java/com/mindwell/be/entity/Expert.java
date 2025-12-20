package com.mindwell.be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "experts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer expertId;

    private String email;
    private String passwordHash;
    private String fullName;
    private String title;
    private BigDecimal hourlyRate;
    private Boolean isVerified;
    private String gender;
}

