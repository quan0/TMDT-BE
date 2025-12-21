package com.mindwell.be.repository;

import com.mindwell.be.entity.ExpertAvailability;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExpertAvailabilityRepository extends JpaRepository<ExpertAvailability, Integer> {

	List<ExpertAvailability> findByExpert_ExpertIdAndIsBookedFalseAndStartTimeBetweenOrderByStartTimeAsc(
			Integer expertId,
			LocalDateTime from,
			LocalDateTime to
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from ExpertAvailability a where a.availabilityId = :id")
	Optional<ExpertAvailability> findByIdForUpdate(@Param("id") Integer id);
}
