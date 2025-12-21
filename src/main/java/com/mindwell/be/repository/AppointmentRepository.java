package com.mindwell.be.repository;

import com.mindwell.be.entity.Appointment;
import com.mindwell.be.entity.enums.AppointmentServiceType;
import com.mindwell.be.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

	@Query("""
			select distinct a.serviceType
			from Appointment a
			where a.serviceType is not null
			order by a.serviceType
			""")
	List<AppointmentServiceType> findDistinctServiceTypes();

	@Query("""
			select a
			from Appointment a
			where a.apptId = :apptId
			  and a.user.userId = :userId
			""")
	Optional<Appointment> findByApptIdAndUserId(@Param("apptId") Integer apptId, @Param("userId") Integer userId);

	Optional<Appointment> findTopByUser_UserIdAndAvailability_AvailabilityIdAndStatusOrderByCreatedAtDesc(
			Integer userId,
			Integer availabilityId,
			AppointmentStatus status
	);

	boolean existsByAvailability_AvailabilityIdAndStatus(Integer availabilityId, AppointmentStatus status);

	@Query("""
			select count(a) > 0
			from Appointment a
			where a.availability.availabilityId = :availabilityId
			  and a.status = com.mindwell.be.entity.enums.AppointmentStatus.PENDING_PROVIDER_PAYMENT
			  and a.createdAt >= :cutoff
			""")
	boolean existsActiveProviderHold(@Param("availabilityId") Integer availabilityId, @Param("cutoff") LocalDateTime cutoff);

	@Query("""
			select count(a) > 0
			from Appointment a
			where a.availability.availabilityId = :availabilityId
			  and a.apptId <> :apptId
			  and (
					a.status = com.mindwell.be.entity.enums.AppointmentStatus.CONFIRMED
					or (a.status = com.mindwell.be.entity.enums.AppointmentStatus.PENDING_PROVIDER_PAYMENT and a.createdAt >= :providerCutoff)
			  )
			""")
	boolean existsOtherActiveReservation(
			@Param("availabilityId") Integer availabilityId,
			@Param("apptId") Integer apptId,
			@Param("providerCutoff") LocalDateTime providerCutoff
	);

	@Query("""
			select a
			from Appointment a
			left join fetch a.expert
			left join fetch a.availability
			left join fetch a.platform
			left join fetch a.payment
			where a.user.userId = :userId
			order by a.startTime desc
			""")
	List<Appointment> findMyAppointmentsWithDetails(@Param("userId") Integer userId);

	@Query("""
			select a
			from Appointment a
			left join fetch a.expert
			left join fetch a.availability
			left join fetch a.platform
			left join fetch a.payment
			where a.user.userId = :userId
			  and a.status = com.mindwell.be.entity.enums.AppointmentStatus.DRAFT
			order by a.createdAt desc
			""")
	List<Appointment> findMyDraftAppointmentsWithDetails(@Param("userId") Integer userId);

	@Query("""
			select a
			from Appointment a
			left join fetch a.availability
			where a.status = com.mindwell.be.entity.enums.AppointmentStatus.PENDING_PROVIDER_PAYMENT
			  and a.createdAt < :cutoff
			""")
	List<Appointment> findExpiredProviderHolds(@Param("cutoff") LocalDateTime cutoff);

	@Query("""
			select a
			from Appointment a
			where a.status = com.mindwell.be.entity.enums.AppointmentStatus.DRAFT
			  and a.payment is null
			  and a.createdAt < :cutoff
			""")
	List<Appointment> findExpiredDraftsWithoutPayment(@Param("cutoff") LocalDateTime cutoff);
}
