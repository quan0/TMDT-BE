package com.mindwell.be.repository;

import com.mindwell.be.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

	@Query("""
			select distinct a.serviceType
			from Appointment a
			where a.serviceType is not null and a.serviceType <> ''
			order by a.serviceType
			""")
	List<String> findDistinctServiceTypes();
}
