package com.mindwell.be.repository;

import com.mindwell.be.entity.Expert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExpertRepository extends JpaRepository<Expert, Integer>, JpaSpecificationExecutor<Expert> {
}
