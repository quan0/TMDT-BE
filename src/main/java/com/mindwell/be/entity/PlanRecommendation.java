package com.mindwell.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plan_recommendations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer recommendationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private TreatmentPlan plan;

    @Column(columnDefinition = "text")
    private String recommendationText;

    private Boolean isCompleted;
}
