package com.mindwell.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "goal_actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer actionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private UserGoal goal;

    @Column(columnDefinition = "text")
    private String actionText;

    private Boolean isCompleted;
}
