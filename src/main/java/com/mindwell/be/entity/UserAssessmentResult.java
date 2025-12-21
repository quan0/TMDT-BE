package com.mindwell.be.entity;

import com.mindwell.be.entity.enums.AssessmentCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_assessment_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAssessmentResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer resultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id")
    private Assessment assessment;

    private Integer totalScore;

    @Enumerated(EnumType.STRING)
    private AssessmentCategory category;
}
