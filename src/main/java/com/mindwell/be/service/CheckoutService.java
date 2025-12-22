package com.mindwell.be.service;

import com.mindwell.be.dto.appointment.MeetingPlatformOptionDto;
import com.mindwell.be.dto.checkout.AppointmentCheckoutDto;
import com.mindwell.be.dto.checkout.AppointmentConfirmationDto;
import com.mindwell.be.dto.checkout.CheckoutOptionsDto;
import com.mindwell.be.dto.payment.InitiateAppointmentPaymentRequest;
import com.mindwell.be.dto.payment.InitiateAppointmentPaymentResponse;
import com.mindwell.be.dto.payment.PaymentMethodOptionDto;
import com.mindwell.be.entity.*;
import com.mindwell.be.entity.enums.AppointmentServiceType;
import com.mindwell.be.entity.enums.AppointmentStatus;
import com.mindwell.be.entity.enums.MindPointTransactionReason;
import com.mindwell.be.entity.enums.PaymentStatus;
import com.mindwell.be.entity.enums.PaymentType;
import com.mindwell.be.repository.*;
import com.mindwell.be.util.MeetingJoinUrlGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutService {
    private final MeetingPlatformRepository meetingPlatformRepository;
    private final AppointmentRepository appointmentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final MindPointTransactionRepository mindPointTransactionRepository;
    private final ExpertAvailabilityRepository expertAvailabilityRepository;
    private final VnpayService vnpayService;

    @Transactional(readOnly = true)
    public CheckoutOptionsDto getCheckoutOptions() {
        List<MeetingPlatformOptionDto> platforms = meetingPlatformRepository.findAll().stream()
                .map(p -> new MeetingPlatformOptionDto(
                        p.getPlatformId(),
                        p.getPlatformKey(),
                        p.getDisplayName(),
                        p.getIsActive()
                ))
                .toList();

        List<String> serviceTypes = appointmentRepository.findDistinctServiceTypes().stream()
                .map(AppointmentServiceType::toJson)
                .toList();
        if (serviceTypes.isEmpty()) {
            serviceTypes = List.of("video", "chat", "voice");
        }

        List<PaymentMethodOptionDto> methods = paymentMethodRepository.findByIsActiveTrueOrderByMethodIdAsc().stream()
                .map(m -> new PaymentMethodOptionDto(m.getMethodKey(), m.getDisplayName(), m.getBadgeLabel(), m.getIsActive()))
                .toList();

        return new CheckoutOptionsDto(platforms, serviceTypes, methods);
    }

    @Transactional(readOnly = true)
    public AppointmentCheckoutDto getAppointmentCheckout(Integer apptId, Integer userId) {
        Appointment appt = appointmentRepository.findById(apptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (appt.getUser() != null && appt.getUser().getUserId() != null && !appt.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found");
        }

        User user = appt.getUser();
        Expert expert = appt.getExpert();
        ExpertAvailability availability = appt.getAvailability();
        MeetingPlatform platform = appt.getPlatform();

        return new AppointmentCheckoutDto(
                appt.getApptId(),
                expert == null ? null : expert.getExpertId(),
                expert == null ? null : expert.getFullName(),
                expert == null ? null : expert.getTitle(),
                platform == null ? null : platform.getPlatformId(),
                platform == null ? null : platform.getDisplayName(),
            appt.getServiceType() == null ? null : appt.getServiceType().toJson(),
                availability == null ? appt.getStartTime() : availability.getStartTime(),
                availability == null ? null : availability.getEndTime(),
                appt.getPaymentAmountPoints(),
                user == null ? null : user.getMindpointsBalance(),
                appt.getContactFullName(),
                appt.getContactEmail(),
                appt.getContactPhone(),
            appt.getStatus() == null ? null : appt.getStatus().toJson()
        );
    }

    @Transactional
    public InitiateAppointmentPaymentResponse payAppointment(Integer apptId, Integer userId, InitiateAppointmentPaymentRequest req, String clientIp) {
        Appointment appt = appointmentRepository.findById(apptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (appt.getUser() != null && appt.getUser().getUserId() != null && !appt.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found");
        }

        if (appt.getUser() == null || appt.getUser().getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment has no user");
        }

        // Only draft appointments are payable
        if (appt.getStatus() != null && appt.getStatus() != AppointmentStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Appointment is not payable");
        }

        if (appt.getAvailability() == null || appt.getAvailability().getAvailabilityId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment has no availability slot");
        }

        // Apply checkout selections (platform/serviceType/contact) before payment
        if (req.platformId() != null) {
            MeetingPlatform platform = meetingPlatformRepository.findById(req.platformId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid platformId"));
            appt.setPlatform(platform);
        } else if (appt.getPlatform() == null) {
            appt.setPlatform(meetingPlatformRepository.findFirstByIsActiveTrueOrderByPlatformIdAsc().orElse(null));
        }

        if (req.serviceType() != null && !req.serviceType().isBlank()) {
            try {
                appt.setServiceType(AppointmentServiceType.fromJson(req.serviceType()));
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
            }
        } else if (appt.getServiceType() == null) {
            appt.setServiceType(AppointmentServiceType.VIDEO);
        }

        if (req.contactFullName() != null && !req.contactFullName().isBlank()) {
            appt.setContactFullName(req.contactFullName());
        }
        if (req.contactEmail() != null && !req.contactEmail().isBlank()) {
            appt.setContactEmail(req.contactEmail());
        }
        if (req.contactPhone() != null && !req.contactPhone().isBlank()) {
            appt.setContactPhone(req.contactPhone());
        }

        // Fill missing contact defaults from user profile
        User apptUser = appt.getUser();
        if (apptUser != null) {
            if (appt.getContactFullName() == null || appt.getContactFullName().isBlank()) {
                appt.setContactFullName(apptUser.getFullName());
            }
            if (appt.getContactEmail() == null || appt.getContactEmail().isBlank()) {
                appt.setContactEmail(apptUser.getEmail());
            }
            if (appt.getContactPhone() == null || appt.getContactPhone().isBlank()) {
                appt.setContactPhone(apptUser.getPhoneNumber());
            }
        }

        // Require contact fields (UI requires name + phone)
        if (appt.getContactFullName() == null || appt.getContactFullName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contactFullName is required");
        }
        if (appt.getContactPhone() == null || appt.getContactPhone().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contactPhone is required");
        }

        if (appt.getPayment() != null && appt.getPayment().getPaymentId() != null) {
            String status = appt.getPayment().getStatus() == null ? null : appt.getPayment().getStatus().toJson();
            return new InitiateAppointmentPaymentResponse(appt.getPayment().getPaymentId(), status, null, "Payment already created");
        }

        // Reserve the slot at pay time (not at draft time)
        Integer availabilityId = appt.getAvailability().getAvailabilityId();
        ExpertAvailability lockedAvailability = expertAvailabilityRepository.findByIdForUpdate(availabilityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Availability slot not found"));

        LocalDateTime providerHoldCutoff = LocalDateTime.now().minusMinutes(15);
        if (appointmentRepository.existsOtherActiveReservation(availabilityId, apptId, providerHoldCutoff)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Availability slot already booked");
        }

        String methodKey = req.methodKey().trim().toLowerCase();
        PaymentMethod method = paymentMethodRepository.findByMethodKey(methodKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment method"));

        int points = appt.getPaymentAmountPoints() == null ? 0 : appt.getPaymentAmountPoints();

        BigDecimal amountVnd = null;
        if (!"mindpoints".equals(methodKey)) {
            // Appointment pricing is computed in VND then converted to points with 1 point ~= 1000 VND.
            amountVnd = BigDecimal.valueOf(points).multiply(BigDecimal.valueOf(1000));
        }

        Payment payment = Payment.builder()
                .user(appt.getUser())
                .amount(amountVnd)
            .status(PaymentStatus.PENDING)
            .paymentType(PaymentType.APPOINTMENT)
                .relatedId(appt.getApptId())
                .method(method)
                .build();

        payment = paymentRepository.save(payment);

        // Block the availability once payment is initiated (holds/confirmed)
        lockedAvailability.setIsBooked(true);
        expertAvailabilityRepository.save(lockedAvailability);

        // MindPoints flow: deduct immediately and mark as paid
        if ("mindpoints".equals(methodKey)) {
            User user = appt.getUser();
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user");
            }

            int balance = user.getMindpointsBalance() == null ? 0 : user.getMindpointsBalance();
            if (points > balance) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient MindPoints");
            }

            user.setMindpointsBalance(balance - points);
            userRepository.save(user);

                payment.setStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);

            mindPointTransactionRepository.save(MindPointTransaction.builder()
                    .user(user)
                    .pointsAmount(-points)
                    .reason(MindPointTransactionReason.APPOINTMENT_PAYMENT)
                    .relatedPayment(payment)
                    .build());

                appt.setStatus(AppointmentStatus.CONFIRMED);
            if (appt.getMeetingJoinUrl() == null || appt.getMeetingJoinUrl().isBlank()) {
                appt.setMeetingJoinUrl(MeetingJoinUrlGenerator.generate(appt.getPlatform()));
            }
        } else {
                appt.setStatus(AppointmentStatus.PENDING_PROVIDER_PAYMENT);
        }

        appt.setPayment(payment);
        appointmentRepository.save(appt);

        // FE needs a consistent next URL after initiating payment.
        // - For MindPoints: payment is immediate, so confirmation is the next step.
        // - For external providers: redirect to a provider payment URL (mock provider for now).
        String redirectUrl;
        if ("mindpoints".equals(methodKey)) {
            redirectUrl = "/api/v1/checkout/appointments/" + apptId + "/confirmation";
        } else if ("vnpay".equals(methodKey)) {
            redirectUrl = vnpayService.createPaymentUrl(payment, clientIp);
        } else {
            redirectUrl = "/api/v1/payments/" + payment.getPaymentId() + "/mock/redirect";
        }

        String paymentStatus = payment.getStatus() == null ? null : payment.getStatus().toJson();
        return new InitiateAppointmentPaymentResponse(payment.getPaymentId(), paymentStatus, redirectUrl,
                "mindpoints".equals(methodKey) ? "Paid with MindPoints" : "Payment initiated");
    }

    @Transactional(readOnly = true)
    public AppointmentConfirmationDto getConfirmation(Integer apptId, Integer userId) {
        Appointment appt = appointmentRepository.findById(apptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (appt.getUser() != null && appt.getUser().getUserId() != null && !appt.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found");
        }

        ExpertAvailability availability = appt.getAvailability();
        LocalDateTime start = availability == null ? appt.getStartTime() : availability.getStartTime();
        LocalDateTime end = availability == null ? null : availability.getEndTime();

        String platformName = appt.getPlatform() == null ? null : appt.getPlatform().getDisplayName();

        return new AppointmentConfirmationDto(
                appt.getApptId(),
            appt.getStatus() == null ? null : appt.getStatus().toJson(),
                start,
                end,
                platformName,
                appt.getMeetingJoinUrl(),
                appt.getContactEmail()
        );
    }

}
