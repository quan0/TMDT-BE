package com.mindwell.be.controller;

import com.mindwell.be.entity.Appointment;
import com.mindwell.be.entity.Payment;
import com.mindwell.be.entity.Subscription;
import com.mindwell.be.entity.UserSubscription;
import com.mindwell.be.entity.enums.AppointmentStatus;
import com.mindwell.be.entity.enums.PaymentStatus;
import com.mindwell.be.entity.enums.PaymentType;
import com.mindwell.be.entity.enums.UserSubscriptionStatus;
import com.mindwell.be.dto.payment.MockPaymentSuccessDto;
import com.mindwell.be.dto.payment.PaymentListItemDto;
import com.mindwell.be.dto.common.PageResponse;
import com.mindwell.be.repository.AppointmentRepository;
import com.mindwell.be.repository.ExpertAvailabilityRepository;
import com.mindwell.be.repository.PaymentRepository;
import com.mindwell.be.repository.SubscriptionRepository;
import com.mindwell.be.repository.UserSubscriptionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import com.mindwell.be.util.MeetingJoinUrlGenerator;

import java.time.LocalDate;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final ExpertAvailabilityRepository expertAvailabilityRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    @GetMapping
    @Operation(
            summary = "List my payments",
            description = "Returns the authenticated user's payments (paged).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @Transactional(readOnly = true)
    public PageResponse<PaymentListItemDto> listMyPayments(
            @AuthenticationPrincipal com.mindwell.be.service.security.UserPrincipal principal,
            @RequestParam(required = false) PaymentType type,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        if (principal == null || principal.getUser() == null || principal.getUser().getUserId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        int safeSize = Math.max(1, Math.min(size, 50));
        Pageable pageable = PageRequest.of(Math.max(0, page), safeSize, Sort.by(Sort.Order.desc("paymentId")));

        Page<Payment> payments = paymentRepository.searchMyPayments(principal.getUser().getUserId(), type, status, pageable);
        Page<PaymentListItemDto> dtoPage = payments.map(p -> new PaymentListItemDto(
                p.getPaymentId(),
                p.getPaymentType() == null ? null : p.getPaymentType().toJson(),
                p.getStatus() == null ? null : p.getStatus().toJson(),
                p.getAmount(),
                p.getRelatedId(),
                p.getMethod() == null ? null : p.getMethod().getMethodKey(),
                p.getMethod() == null ? null : p.getMethod().getDisplayName()
        ));

        return PageResponse.from(dtoPage);
    }

    @GetMapping("/{paymentId}/mock/redirect")
    @Operation(
        summary = "Mock payment provider redirect",
        description = "Development-only: simulates an external payment provider by marking the payment as PAID and returning a success JSON payload."
    )
    @Transactional
    public MockPaymentSuccessDto mockProviderRedirect(@PathVariable Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        if (payment.getPaymentType() == PaymentType.APPOINTMENT) {
            if (payment.getRelatedId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment has no relatedId");
            }

            Appointment appt = appointmentRepository.findById(payment.getRelatedId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

            // If payment already paid, just redirect
            if (payment.getStatus() != PaymentStatus.PAID) {
                payment.setStatus(PaymentStatus.PAID);
                paymentRepository.save(payment);

                appt.setStatus(AppointmentStatus.CONFIRMED);
                if (appt.getMeetingJoinUrl() == null || appt.getMeetingJoinUrl().isBlank()) {
                    // Generate on confirm for external providers
                    appt.setMeetingJoinUrl(MeetingJoinUrlGenerator.generate(appt.getPlatform()));
                }
                appointmentRepository.save(appt);
            }

            // Ensure availability remains booked when confirmed
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

        if (payment.getPaymentType() == PaymentType.SUBSCRIPTION) {
            if (payment.getRelatedId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment has no relatedId");
            }
            if (payment.getUser() == null || payment.getUser().getUserId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment has no user");
            }

            // Avoid duplicates if redirect is hit multiple times
            if (userSubscriptionRepository.findByPaymentPaymentId(paymentId).isPresent()) {
                UserSubscription existing = userSubscriptionRepository.findByPaymentPaymentId(paymentId).orElse(null);
                return new MockPaymentSuccessDto(
                        payment.getPaymentId(),
                        payment.getPaymentType() == null ? null : payment.getPaymentType().toJson(),
                        payment.getStatus() == null ? null : payment.getStatus().toJson(),
                        payment.getRelatedId(),
                        existing == null ? null : existing.getUserSubId(),
                        existing == null ? null : existing.getExpiryDate()
                );
            }

            Subscription plan = subscriptionRepository.findById(payment.getRelatedId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription plan not found"));

            if (payment.getStatus() != PaymentStatus.PAID) {
                payment.setStatus(PaymentStatus.PAID);
                paymentRepository.save(payment);
            }

            LocalDate today = LocalDate.now();

            // Safety: expire any current active subscriptions
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

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment type");
    }
}
