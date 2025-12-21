package com.mindwell.be.controller;

import com.mindwell.be.dto.checkout.AppointmentCheckoutDto;
import com.mindwell.be.dto.checkout.AppointmentConfirmationDto;
import com.mindwell.be.dto.checkout.CheckoutOptionsDto;
import com.mindwell.be.dto.payment.InitiateAppointmentPaymentRequest;
import com.mindwell.be.dto.payment.InitiateAppointmentPaymentResponse;
import com.mindwell.be.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/checkout", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    @GetMapping("/options")
    @Operation(
            summary = "Get checkout options",
            description = "Returns platforms, serviceTypes and payment methods used by the booking/checkout UI."
    )
    public CheckoutOptionsDto getOptions() {
        return checkoutService.getCheckoutOptions();
    }

    @GetMapping("/appointments/{apptId}/checkout")
    @Operation(
            summary = "Get appointment checkout summary",
            description = "Returns the info needed to render the checkout summary (expert, slot, totals, user balance).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public AppointmentCheckoutDto getAppointmentCheckout(
            @PathVariable Integer apptId,
            @AuthenticationPrincipal com.mindwell.be.service.security.UserPrincipal principal
    ) {
        if (principal == null || principal.getUser() == null || principal.getUser().getUserId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return checkoutService.getAppointmentCheckout(apptId, principal.getUser().getUserId());
    }

    @PostMapping(value = "/appointments/{apptId}/payments", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Pay for an appointment",
            description = "Creates a payment for the appointment. If methodKey=mindpoints, it deducts points and confirms immediately; otherwise it creates a pending payment (provider integration is a placeholder).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public InitiateAppointmentPaymentResponse payAppointment(
            @PathVariable Integer apptId,
            @AuthenticationPrincipal com.mindwell.be.service.security.UserPrincipal principal,
            @Valid @RequestBody InitiateAppointmentPaymentRequest req
    ) {
        if (principal == null || principal.getUser() == null || principal.getUser().getUserId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return checkoutService.payAppointment(apptId, principal.getUser().getUserId(), req);
    }

    @GetMapping("/appointments/{apptId}/confirmation")
    @Operation(
            summary = "Get appointment confirmation",
            description = "Returns data for the success modal (meeting link, time, email).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public AppointmentConfirmationDto confirmation(
            @PathVariable Integer apptId,
            @AuthenticationPrincipal com.mindwell.be.service.security.UserPrincipal principal
    ) {
        if (principal == null || principal.getUser() == null || principal.getUser().getUserId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return checkoutService.getConfirmation(apptId, principal.getUser().getUserId());
    }
}
