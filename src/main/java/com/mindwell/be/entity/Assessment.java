package com.mindwell.be.entity;

import com.mindwell.be.entity.enums.AssessmentCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "assessments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_assessments_category", columnNames = {"category"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer assessmentId;

    private String title;

        @Enumerated(EnumType.STRING)
        private AssessmentCategory category;
}
