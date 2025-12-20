package com.mindwell.be.service;

import com.mindwell.be.dto.appointment.AppointmentOptionsDto;
import com.mindwell.be.dto.appointment.CreateAppointmentRequest;
import com.mindwell.be.dto.appointment.CreateAppointmentResponse;
import com.mindwell.be.dto.appointment.MeetingPlatformOptionDto;
import com.mindwell.be.entity.*;
import com.mindwell.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

                List<String> serviceTypes = appointmentRepository.findDistinctServiceTypes();
                return new AppointmentOptionsDto(platforms, serviceTypes);
        }

    @Transactional
    public CreateAppointmentResponse createAppointment(Integer expertId, Integer userId, CreateAppointmentRequest req) {
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

        if (Boolean.TRUE.equals(availability.getIsBooked())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Availability slot already booked");
        }

        MeetingPlatform platform = null;
        if (req.platformId() != null) {
            platform = meetingPlatformRepository.findById(req.platformId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid platformId"));
        } else {
            platform = meetingPlatformRepository.findFirstByIsActiveTrueOrderByPlatformIdAsc().orElse(null);
        }

        String serviceType = (req.serviceType() == null || req.serviceType().isBlank()) ? "video" : req.serviceType();

        Appointment appointment = Appointment.builder()
                .user(user)
                .expert(expert)
                .availability(availability)
                .startTime(availability.getStartTime())
                .status("pending")
                .serviceType(serviceType)
                .paymentAmountPoints(0)
                .platform(platform)
                .build();

        availability.setIsBooked(true);
        expertAvailabilityRepository.save(availability);

        appointment = appointmentRepository.save(appointment);

        return new CreateAppointmentResponse(
                appointment.getApptId(),
                expertId,
                userId,
                availability.getAvailabilityId(),
                appointment.getStartTime(),
                appointment.getStatus()
        );
    }
}
