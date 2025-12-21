package com.mindwell.be.entity;

import com.mindwell.be.entity.enums.TreatmentPlanStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "treatment_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreatmentPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer planId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id")
    private Expert expert;

    private String diagnosisTitle;

    @Enumerated(EnumType.STRING)
    private TreatmentPlanStatus status;
}
