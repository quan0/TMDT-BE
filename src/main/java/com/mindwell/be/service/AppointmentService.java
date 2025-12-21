package com.mindwell.be.service;

import com.mindwell.be.dto.appointment.AppointmentOptionsDto;
import com.mindwell.be.dto.appointment.CreateAppointmentRequest;
import com.mindwell.be.dto.appointment.CreateAppointmentResponse;
import com.mindwell.be.dto.appointment.MeetingPlatformOptionDto;
import com.mindwell.be.entity.*;
import com.mindwell.be.entity.enums.AppointmentServiceType;
import com.mindwell.be.entity.enums.AppointmentStatus;
import com.mindwell.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final ExpertRepository expertRepository;
    private final UserRepository userRepository;
    private final ExpertAvailabilityRepository expertAvailabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final MeetingPlatformRepository meetingPlatformRepository;

    @Transactional(readOnly = true)
    public AppointmentOptionsDto getAppointmentOptions() {
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
            return new AppointmentOptionsDto(platforms, serviceTypes);
    }

    @Transactional
    public CreateAppointmentResponse createAppointment(Integer expertId, Integer userId, CreateAppointmentRequest req) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Expert expert = expertRepository.findById(expertId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expert not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ExpertAvailability availability = expertAvailabilityRepository.findById(req.availabilityId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Availability slot not found"));

        if (availability.getExpert() == null || availability.getExpert().getExpertId() == null
                || !availability.getExpert().getExpertId().equals(expertId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Availability slot does not belong to this expert");
        }

                LocalDateTime providerHoldCutoff = LocalDateTime.now().minusMinutes(15);
                if (appointmentRepository.existsByAvailability_AvailabilityIdAndStatus(availability.getAvailabilityId(), AppointmentStatus.CONFIRMED)
                                || appointmentRepository.existsActiveProviderHold(availability.getAvailabilityId(), providerHoldCutoff)
                                || Boolean.TRUE.equals(availability.getIsBooked())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Availability slot already booked");
                }

                // Idempotent draft per (user, availability): if a recent draft exists, return it
                LocalDateTime draftCutoff = LocalDateTime.now().minusHours(2);
                var existingDraft = appointmentRepository
                                .findTopByUser_UserIdAndAvailability_AvailabilityIdAndStatusOrderByCreatedAtDesc(userId, availability.getAvailabilityId(), AppointmentStatus.DRAFT)
                                .filter(a -> a.getCreatedAt() != null && !a.getCreatedAt().isBefore(draftCutoff));
                if (existingDraft.isPresent()) {
                        Appointment a = existingDraft.get();
                        return new CreateAppointmentResponse(
                                        a.getApptId(),
                                        expertId,
                                        userId,
                                        availability.getAvailabilityId(),
                                        a.getStartTime(),
                                        a.getStatus() == null ? null : a.getStatus().toJson(),
                                        a.getPaymentAmountPoints(),
                                        a.getServiceType() == null ? null : a.getServiceType().toJson(),
                                        a.getPlatform() == null ? null : a.getPlatform().getPlatformId()
                        );
                }

        // Draft creation intentionally does not bind platform/serviceType/contact.
        // Those are collected on the checkout page and applied during payment.
        MeetingPlatform platform = null;
        AppointmentServiceType serviceType = null;

        int durationMinutes = 60;
        if (availability.getStartTime() != null && availability.getEndTime() != null) {
            durationMinutes = (int) Math.max(1, Duration.between(availability.getStartTime(), availability.getEndTime()).toMinutes());
        }

        BigDecimal hourlyRate = expert.getHourlyRate() == null ? BigDecimal.ZERO : expert.getHourlyRate();
        BigDecimal minutes = BigDecimal.valueOf(durationMinutes);
        BigDecimal amountVnd = hourlyRate
                .multiply(minutes)
                .divide(BigDecimal.valueOf(60), 0, RoundingMode.HALF_UP);

        int amountPoints = amountVnd
                .divide(BigDecimal.valueOf(1000), 0, RoundingMode.HALF_UP)
                .intValue();

        Appointment appointment = Appointment.builder()
                .user(user)
                .expert(expert)
                .availability(availability)
                .startTime(availability.getStartTime())
                .status(AppointmentStatus.DRAFT)
                .serviceType(serviceType)
                .paymentAmountPoints(amountPoints)
                .platform(platform)
                .build();

        appointment = appointmentRepository.save(appointment);

        return new CreateAppointmentResponse(
                appointment.getApptId(),
                expertId,
                user.getUserId(),
                availability.getAvailabilityId(),
                appointment.getStartTime(),
                appointment.getStatus() == null ? null : appointment.getStatus().toJson(),
                appointment.getPaymentAmountPoints(),
                appointment.getServiceType() == null ? null : appointment.getServiceType().toJson(),
                appointment.getPlatform() == null ? null : appointment.getPlatform().getPlatformId()
        );
    }

        @Transactional
        public CreateAppointmentResponse createAppointmentDraft(Integer expertId, Integer userId, Integer availabilityId) {
                return createAppointment(
                                expertId,
                                userId,
                                new CreateAppointmentRequest(
                                                availabilityId,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null
                                )
                );
        }
}
