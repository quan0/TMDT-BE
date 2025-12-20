package com.mindwell.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer goalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private TreatmentPlan plan;

    private String title;
    private String currentValue;
    private String targetValue;

    @Column(name = "assessment_category", insertable = false, updatable = false)
    private String assessmentCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_category", referencedColumnName = "category")
    private Assessment assessment;
}
