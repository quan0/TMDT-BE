package com.mindwell.be.repository;

import com.mindwell.be.entity.ExpertLanguage;
import com.mindwell.be.entity.ExpertLanguageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpertLanguageRepository extends JpaRepository<ExpertLanguage, ExpertLanguageId> {

	interface ExpertLanguageRow {
		Integer getExpertId();

		String getLangCode();

		String getName();
	}

	@Query("""
			select el.expert.expertId as expertId,
				   el.language.langCode as langCode,
				   el.language.name as name
			from ExpertLanguage el
			where el.expert.expertId in :expertIds
			""")
	List<ExpertLanguageRow> findLanguageRowsByExpertIds(@Param("expertIds") List<Integer> expertIds);
}
