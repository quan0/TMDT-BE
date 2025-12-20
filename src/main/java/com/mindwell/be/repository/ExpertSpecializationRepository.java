package com.mindwell.be.repository;

import com.mindwell.be.entity.ExpertSpecialization;
import com.mindwell.be.entity.ExpertSpecializationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpertSpecializationRepository extends JpaRepository<ExpertSpecialization, ExpertSpecializationId> {

	interface ExpertSpecializationRow {
		Integer getExpertId();

		Integer getSpecId();

		String getName();
	}

	@Query("""
			select es.expert.expertId as expertId,
				   es.specialization.specId as specId,
				   es.specialization.name as name
			from ExpertSpecialization es
			where es.expert.expertId in :expertIds
			""")
	List<ExpertSpecializationRow> findSpecializationRowsByExpertIds(@Param("expertIds") List<Integer> expertIds);
}
