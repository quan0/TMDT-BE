package com.mindwell.be.service;

import com.mindwell.be.dto.subscription.*;
import com.mindwell.be.entity.Payment;
import com.mindwell.be.entity.PaymentMethod;
import com.mindwell.be.entity.Subscription;
import com.mindwell.be.entity.User;
import com.mindwell.be.entity.UserSubscription;
import com.mindwell.be.entity.enums.PaymentStatus;
import com.mindwell.be.entity.enums.PaymentType;
import com.mindwell.be.entity.enums.SubscriptionBillingCycle;
import com.mindwell.be.repository.PaymentMethodRepository;
import com.mindwell.be.repository.PaymentRepository;
import com.mindwell.be.repository.SubscriptionRepository;
import com.mindwell.be.repository.UserRepository;
import com.mindwell.be.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SubscriptionPlanDto> listPlans() {
        return subscriptionRepository.findByIsActiveTrueOrderByPriceAsc().stream()
            .map(this::toPlanDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public MySubscriptionDto getMySubscription(Integer userId) {
        LocalDate today = LocalDate.now();
        UserSubscription us = userSubscriptionRepository.findActiveByUserId(userId, today)
                .stream()
                .findFirst()
                .orElse(null);

        if (us == null) {
            return new MySubscriptionDto(null, null, null, null);
        }

        Integer paymentId = us.getPayment() == null ? null : us.getPayment().getPaymentId();
        return new MySubscriptionDto(
                toPlanDto(us.getSubscription()),
                us.getStatus() == null ? null : us.getStatus().toJson(),
                us.getExpiryDate(),
                paymentId
        );
    }

    @Transactional
    public InitiateSubscriptionPaymentResponse initiateSubscriptionPayment(Integer subId, Integer userId, InitiateSubscriptionPaymentRequest req) {
        Subscription plan = subscriptionRepository.findById(subId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription plan not found"));

        if (plan.getIsActive() != null && !plan.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subscription plan is not active");
        }

        String methodKey = req.methodKey().trim().toLowerCase();
        if ("mindpoints".equals(methodKey)) {
            // Not implemented in this backend yet: MindPoints are used for appointment payments.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mindpoints is not supported for subscriptions");
        }

        PaymentMethod method = paymentMethodRepository.findByMethodKey(methodKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment method"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        // If user already has an active subscription, keep it simple for now.
        LocalDate today = LocalDate.now();
        boolean hasActive = !userSubscriptionRepository.findActiveByUserId(userId, today).isEmpty();
        if (hasActive) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already has an active subscription");
        }

        Payment payment = Payment.builder()
                .user(user)
                .amount(plan.getPrice())
                .status(PaymentStatus.PENDING)
                .paymentType(PaymentType.SUBSCRIPTION)
                // relatedId points to subscription plan; the mock provider will create UserSubscription on success.
                .relatedId(plan.getSubId())
                .method(method)
                .build();

        payment = paymentRepository.save(payment);

        String redirectUrl = "/api/v1/payments/" + payment.getPaymentId() + "/mock/redirect";
        return new InitiateSubscriptionPaymentResponse(
                payment.getPaymentId(),
                payment.getStatus() == null ? null : payment.getStatus().toJson(),
                redirectUrl,
                "Payment initiated"
        );
    }

    @Transactional(readOnly = true)
    public SubscriptionConfirmationDto confirmSubscriptionPayment(Integer userId, Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        if (payment.getUser() == null || payment.getUser().getUserId() == null || !payment.getUser().getUserId().equals(userId)) {
            // Do not leak existence of other users' payments
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
        }
        if (payment.getPaymentType() != PaymentType.SUBSCRIPTION) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment is not a subscription payment");
        }
        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment is not paid yet");
        }

        UserSubscription us = userSubscriptionRepository.findByPaymentPaymentId(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found for payment"));

        return new SubscriptionConfirmationDto(
                toPlanDto(us.getSubscription()),
                us.getStatus() == null ? null : us.getStatus().toJson(),
                us.getExpiryDate(),
                payment.getPaymentId(),
                payment.getStatus() == null ? null : payment.getStatus().toJson()
        );
    }

    private SubscriptionPlanDto toPlanDto(Subscription s) {
        if (s == null) return null;
        List<String> features = parseFeatures(s.getFeatures());
        SubscriptionBillingCycle cycle = s.getBillingCycle();
        return new SubscriptionPlanDto(
                s.getSubId(),
                s.getName(),
                s.getPrice(),
                cycle == null ? null : cycle.toJson(),
                s.getTierSubtitle(),
                s.getBadgeLabel(),
                s.getShortDesc(),
                features,
                s.getIsActive()
        );
    }

    private static List<String> parseFeatures(String features) {
        if (features == null || features.isBlank()) return List.of();
        return Arrays.stream(features.split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
