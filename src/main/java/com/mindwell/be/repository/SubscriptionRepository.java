package com.mindwell.be.repository;

import com.mindwell.be.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
	List<Subscription> findByIsActiveTrueOrderByPriceAsc();
}
