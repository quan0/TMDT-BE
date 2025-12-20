package com.mindwell.be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    private String email;
    private String passwordHash;
    private String fullName;
    private String phoneNumber;
    private Integer mindpointsBalance;
    private LocalDate memberSince;
}

