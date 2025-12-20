package com.mindwell.be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "goal_milestones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalMilestone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer milestoneId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private UserGoal goal;

    private String milestoneText;
    private LocalDate achievedDate;
}
