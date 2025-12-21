package com.mindwell.be.controller;

import com.mindwell.be.dto.subscription.*;
import com.mindwell.be.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/plans")
    @Operation(
            summary = "List subscription plans",
            description = "Returns the membership plans shown on the pricing page (Free/Premium/Platinum)."
    )
    public List<SubscriptionPlanDto> listPlans() {
        return subscriptionService.listPlans();
    }

    @GetMapping("/my")
    @Operation(
            summary = "Get my active subscription",
            description = "Returns the current active subscription for the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public MySubscriptionDto mySubscription(
            @AuthenticationPrincipal com.mindwell.be.service.security.UserPrincipal principal
    ) {
        if (principal == null || principal.getUser() == null || principal.getUser().getUserId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return subscriptionService.getMySubscription(principal.getUser().getUserId());
    }

    @GetMapping("/confirmation")
    @Operation(
            summary = "Confirm subscription payment",
            description = "Given a paymentId, returns the activated subscription details for the authenticated user. Use this after provider redirect.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public SubscriptionConfirmationDto confirm(
            @RequestParam Integer paymentId,
            @AuthenticationPrincipal com.mindwell.be.service.security.UserPrincipal principal
    ) {
        if (principal == null || principal.getUser() == null || principal.getUser().getUserId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return subscriptionService.confirmSubscriptionPayment(principal.getUser().getUserId(), paymentId);
    }

    @PostMapping(value = "/{subId}/payments", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Initiate subscription payment",
            description = "Creates a payment for a subscription plan (provider integration is mocked).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public InitiateSubscriptionPaymentResponse initiatePayment(
            @PathVariable Integer subId,
            @AuthenticationPrincipal com.mindwell.be.service.security.UserPrincipal principal,
            @Valid @RequestBody InitiateSubscriptionPaymentRequest req
    ) {
        if (principal == null || principal.getUser() == null || principal.getUser().getUserId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return subscriptionService.initiateSubscriptionPayment(subId, principal.getUser().getUserId(), req);
    }
}
