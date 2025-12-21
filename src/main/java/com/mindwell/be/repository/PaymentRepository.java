package com.mindwell.be.repository;

import com.mindwell.be.entity.Payment;
import com.mindwell.be.entity.enums.PaymentStatus;
import com.mindwell.be.entity.enums.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
	Page<Payment> findByUserUserIdOrderByPaymentIdDesc(Integer userId, Pageable pageable);

	@Query("""
		select p
		from Payment p
		where p.user.userId = :userId
		  and (:type is null or p.paymentType = :type)
		  and (:status is null or p.status = :status)
		order by p.paymentId desc
		""")
	Page<Payment> searchMyPayments(
			@Param("userId") Integer userId,
			@Param("type") PaymentType type,
			@Param("status") PaymentStatus status,
			Pageable pageable
	);
}
