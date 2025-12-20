package com.mindwell.be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "expert_availability")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer availabilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id")
    private Expert expert;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isBooked;
}
