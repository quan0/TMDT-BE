package com.mindwell.be.controller;

import com.mindwell.be.entity.Appointment;
import com.mindwell.be.entity.Payment;
import com.mindwell.be.entity.enums.AppointmentStatus;
import com.mindwell.be.entity.enums.PaymentStatus;
import com.mindwell.be.entity.enums.PaymentType;
import com.mindwell.be.repository.AppointmentRepository;
import com.mindwell.be.repository.ExpertAvailabilityRepository;
import com.mindwell.be.repository.PaymentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;
import com.mindwell.be.util.MeetingJoinUrlGenerator;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final ExpertAvailabilityRepository expertAvailabilityRepository;

    @GetMapping("/{paymentId}/mock/redirect")
    @Operation(
        summary = "Mock payment provider redirect",
        description = "Development-only: simulates an external payment provider by marking the payment as PAID and redirecting to the appointment confirmation URL."
    )
    @Transactional
    public RedirectView mockProviderRedirect(@PathVariable Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        if (payment.getPaymentType() != PaymentType.APPOINTMENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment type");
        }
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

        RedirectView rv = new RedirectView();
        rv.setUrl("/api/v1/checkout/appointments/" + appt.getApptId() + "/confirmation");
        rv.setExposeModelAttributes(false);
        return rv;
    }
}
