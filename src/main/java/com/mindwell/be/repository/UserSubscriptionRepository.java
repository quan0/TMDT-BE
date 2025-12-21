package com.mindwell.be.repository;

import com.mindwell.be.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Integer> {
	Optional<UserSubscription> findByPaymentPaymentId(Integer paymentId);

	@Query("""
		select us
		from UserSubscription us
		join fetch us.subscription s
		left join fetch us.payment p
		where us.user.userId = :userId
		  and us.status = com.mindwell.be.entity.enums.UserSubscriptionStatus.ACTIVE
		  and (us.expiryDate is null or us.expiryDate >= :today)
		order by coalesce(us.expiryDate, :farFuture) desc, us.userSubId desc
		""")
	List<UserSubscription> findActiveByUserId(
			@Param("userId") Integer userId,
			@Param("today") LocalDate today,
			@Param("farFuture") LocalDate farFuture
	);

	default List<UserSubscription> findActiveByUserId(Integer userId, LocalDate today) {
		return findActiveByUserId(userId, today, LocalDate.of(9999, 12, 31));
	}
}
