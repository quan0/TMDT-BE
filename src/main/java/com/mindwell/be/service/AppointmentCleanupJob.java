package com.mindwell.be.service;

import com.mindwell.be.entity.Appointment;
import com.mindwell.be.entity.ExpertAvailability;
import com.mindwell.be.entity.enums.AppointmentStatus;
import com.mindwell.be.repository.AppointmentRepository;
import com.mindwell.be.repository.ExpertAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentCleanupJob {

    private final AppointmentRepository appointmentRepository;
    private final ExpertAvailabilityRepository expertAvailabilityRepository;

    private static final long PROVIDER_HOLD_TTL_MINUTES = 15;
    private static final long DRAFT_TTL_HOURS = 2;

    @Scheduled(fixedDelayString = "${app.appointments.cleanup.fixed-delay-ms:60000}")
    @Transactional
    public void cleanupExpiredAppointments() {
        LocalDateTime now = LocalDateTime.now();

        // Release expired provider-payment holds
        LocalDateTime providerCutoff = now.minusMinutes(PROVIDER_HOLD_TTL_MINUTES);
        List<Appointment> expiredHolds = appointmentRepository.findExpiredProviderHolds(providerCutoff);
        for (Appointment appt : expiredHolds) {
            appt.setStatus(AppointmentStatus.CANCELLED);

            ExpertAvailability availability = appt.getAvailability();
            if (availability != null) {
                availability.setIsBooked(false);
                expertAvailabilityRepository.save(availability);
            }
        }
        if (!expiredHolds.isEmpty()) {
            appointmentRepository.saveAll(expiredHolds);
        }

        // Expire old drafts (does not affect availability)
        LocalDateTime draftCutoff = now.minusHours(DRAFT_TTL_HOURS);
        List<Appointment> expiredDrafts = appointmentRepository.findExpiredDraftsWithoutPayment(draftCutoff);
        for (Appointment appt : expiredDrafts) {
            appt.setStatus(AppointmentStatus.CANCELLED);
        }
        if (!expiredDrafts.isEmpty()) {
            appointmentRepository.saveAll(expiredDrafts);
        }
    }
}
