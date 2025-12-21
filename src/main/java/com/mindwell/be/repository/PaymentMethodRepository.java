package com.mindwell.be.repository;

import com.mindwell.be.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {
	List<PaymentMethod> findByIsActiveTrueOrderByMethodIdAsc();
	java.util.Optional<PaymentMethod> findByMethodKey(String methodKey);
}
