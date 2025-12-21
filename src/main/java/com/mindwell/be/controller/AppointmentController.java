package com.mindwell.be.controller;

import com.mindwell.be.dto.appointment.MyAppointmentItemDto;
import com.mindwell.be.entity.Appointment;
import com.mindwell.be.entity.Expert;
import com.mindwell.be.entity.ExpertAvailability;
import com.mindwell.be.entity.MeetingPlatform;
import com.mindwell.be.entity.Payment;
import com.mindwell.be.repository.AppointmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/appointments", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Appointments")
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;

    @GetMapping("/my")
    @Operation(
            summary = "List my appointments",
            description = "Returns the authenticated user's appointments so the UI/debugging can see apptId and status.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public List<MyAppointmentItemDto> myAppointments(
            @AuthenticationPrincipal com.mindwell.be.service.security.UserPrincipal principal
    ) {
        if (principal == null || principal.getUser() == null || principal.getUser().getUserId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Integer userId = principal.getUser().getUserId();

        return appointmentRepository.findMyAppointmentsWithDetails(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

        @GetMapping("/my/drafts")
        @Operation(
            summary = "List my draft appointments",
            description = "Returns the authenticated user's draft appointments so the UI can resume checkout after reload.",
            security = @SecurityRequirement(name = "bearerAuth")
        )
        public List<MyAppointmentItemDto> myDraftAppointments(
            @AuthenticationPrincipal com.mindwell.be.service.security.UserPrincipal principal
        ) {
        if (principal == null || principal.getUser() == null || principal.getUser().getUserId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Integer userId = principal.getUser().getUserId();
        return appointmentRepository.findMyDraftAppointmentsWithDetails(userId)
            .stream()
            .map(this::toDto)
            .toList();
        }

    private MyAppointmentItemDto toDto(Appointment appt) {
        Expert expert = appt.getExpert();
        ExpertAvailability availability = appt.getAvailability();
        MeetingPlatform platform = appt.getPlatform();
        Payment payment = appt.getPayment();

        return new MyAppointmentItemDto(
                appt.getApptId(),
                expert == null ? null : expert.getExpertId(),
                expert == null ? null : expert.getFullName(),
                expert == null ? null : expert.getTitle(),
                availability == null ? appt.getStartTime() : availability.getStartTime(),
                availability == null ? null : availability.getEndTime(),
                appt.getStatus() == null ? null : appt.getStatus().toJson(),
                appt.getServiceType() == null ? null : appt.getServiceType().toJson(),
                platform == null ? null : platform.getPlatformId(),
                platform == null ? null : platform.getDisplayName(),
                appt.getPaymentAmountPoints(),
                payment == null ? null : payment.getPaymentId(),
                payment == null || payment.getStatus() == null ? null : payment.getStatus().toJson(),
                appt.getMeetingJoinUrl()
        );
    }
}
