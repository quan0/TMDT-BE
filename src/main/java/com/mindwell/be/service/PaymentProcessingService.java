package com.mindwell.be.service;

import com.mindwell.be.dto.payment.MockPaymentSuccessDto;
import com.mindwell.be.entity.*;
import com.mindwell.be.entity.enums.*;
import com.mindwell.be.repository.*;
import com.mindwell.be.util.MeetingJoinUrlGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PaymentProcessingService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final ExpertAvailabilityRepository expertAvailabilityRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    @Transactional
    public MockPaymentSuccessDto markPaidAndFinalize(Payment payment) {
        if (payment == null || payment.getPaymentId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment");
        }

        if (payment.getPaymentType() == PaymentType.APPOINTMENT) {
            return finalizeAppointmentPayment(payment);
        }

        if (payment.getPaymentType() == PaymentType.SUBSCRIPTION) {
            return finalizeSubscriptionPayment(payment);
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment type");
    }

    @Transactional
    public void markFailedIfPending(Payment payment) {
        if (payment == null || payment.getPaymentId() == null) return;
        if (payment.getStatus() == PaymentStatus.PAID) return;

        if (payment.getStatus() != PaymentStatus.FAILED) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }

        if (payment.getPaymentType() == PaymentType.APPOINTMENT && payment.getRelatedId() != null) {
            Appointment appt = appointmentRepository.findById(payment.getRelatedId()).orElse(null);
            if (appt != null && appt.getStatus() == AppointmentStatus.PENDING_PROVIDER_PAYMENT) {
                appt.setStatus(AppointmentStatus.DRAFT);
                appointmentRepository.save(appt);

                if (appt.getAvailability() != null) {
                    var availability = appt.getAvailability();
                    availability.setIsBooked(false);
                    expertAvailabilityRepository.save(availability);
                }
            }
        }
    }

    private MockPaymentSuccessDto finalizeAppointmentPayment(Payment payment) {
        if (payment.getRelatedId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment has no relatedId");
        }

        Appointment appt = appointmentRepository.findById(payment.getRelatedId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (payment.getStatus() != PaymentStatus.PAID) {
            payment.setStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);

            appt.setStatus(AppointmentStatus.CONFIRMED);
            if (appt.getMeetingJoinUrl() == null || appt.getMeetingJoinUrl().isBlank()) {
                appt.setMeetingJoinUrl(MeetingJoinUrlGenerator.generate(appt.getPlatform()));
            }
            appointmentRepository.save(appt);
        }

        if (appt.getAvailability() != null) {
            var availability = appt.getAvailability();
            availability.setIsBooked(true);
            expertAvailabilityRepository.save(availability);
        }

        return new MockPaymentSuccessDto(
                payment.getPaymentId(),
                payment.getPaymentType() == null ? null : payment.getPaymentType().toJson(),
                payment.getStatus() == null ? null : payment.getStatus().toJson(),
                payment.getRelatedId(),
                null,
                null
        );
    }

    private MockPaymentSuccessDto finalizeSubscriptionPayment(Payment payment) {
        if (payment.getRelatedId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment has no relatedId");
        }
        if (payment.getUser() == null || payment.getUser().getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment has no user");
        }

        var existingOpt = userSubscriptionRepository.findByPaymentPaymentId(payment.getPaymentId());
        if (existingOpt.isPresent()) {
            UserSubscription existing = existingOpt.get();
            return new MockPaymentSuccessDto(
                    payment.getPaymentId(),
                    payment.getPaymentType() == null ? null : payment.getPaymentType().toJson(),
                    payment.getStatus() == null ? null : payment.getStatus().toJson(),
                    payment.getRelatedId(),
                    existing.getUserSubId(),
                    existing.getExpiryDate()
            );
        }

        Subscription plan = subscriptionRepository.findById(payment.getRelatedId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription plan not found"));

        if (payment.getStatus() != PaymentStatus.PAID) {
            payment.setStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);
        }

        LocalDate today = LocalDate.now();

        var currentActive = userSubscriptionRepository.findActiveByUserId(payment.getUser().getUserId(), today);
        if (!currentActive.isEmpty()) {
            for (UserSubscription us : currentActive) {
                us.setStatus(UserSubscriptionStatus.EXPIRED);
                us.setExpiryDate(today.minusDays(1));
            }
            userSubscriptionRepository.saveAll(currentActive);
        }

        LocalDate expiryDate = null;
        if (plan.getBillingCycle() != null) {
            expiryDate = switch (plan.getBillingCycle()) {
                case YEARLY -> today.plusYears(1);
                case MONTHLY -> today.plusMonths(1);
            };
        }

        UserSubscription userSubscription = UserSubscription.builder()
                .user(payment.getUser())
                .subscription(plan)
                .payment(payment)
                .status(UserSubscriptionStatus.ACTIVE)
                .expiryDate(expiryDate)
                .build();
        userSubscriptionRepository.save(userSubscription);

        return new MockPaymentSuccessDto(
                payment.getPaymentId(),
                payment.getPaymentType() == null ? null : payment.getPaymentType().toJson(),
                payment.getStatus() == null ? null : payment.getStatus().toJson(),
                payment.getRelatedId(),
                userSubscription.getUserSubId(),
                userSubscription.getExpiryDate()
        );
    }
}
