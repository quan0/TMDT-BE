package com.mindwell.be.repository;

import com.mindwell.be.entity.ExpertAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ExpertAvailabilityRepository extends JpaRepository<ExpertAvailability, Integer> {

	List<ExpertAvailability> findByExpert_ExpertIdAndIsBookedFalseAndStartTimeBetweenOrderByStartTimeAsc(
			Integer expertId,
			LocalDateTime from,
			LocalDateTime to
	);
}
