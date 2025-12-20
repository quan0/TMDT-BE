package com.mindwell.be.repository;

import com.mindwell.be.entity.UserGoal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGoalRepository extends JpaRepository<UserGoal, Integer> {
}
