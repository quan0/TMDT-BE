package com.mindwell.be.controller;

import com.mindwell.be.dto.common.PageResponse;
import com.mindwell.be.dto.appointment.AppointmentOptionsDto;
import com.mindwell.be.dto.appointment.CreateAppointmentDraftRequest;
import com.mindwell.be.dto.appointment.CreateAppointmentResponse;
import com.mindwell.be.dto.expert.AvailabilitySlotDto;
import com.mindwell.be.dto.expert.ExpertCardDto;
import com.mindwell.be.dto.expert.ExpertDetailDto;
import com.mindwell.be.dto.expert.ExpertFilterOptionsDto;
import com.mindwell.be.dto.expert.ExpertReviewDto;
import com.mindwell.be.service.AppointmentService;
import com.mindwell.be.service.ExpertDetailService;
import com.mindwell.be.service.ExpertService;
import com.mindwell.be.service.spec.ExpertSearchCriteria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@RestController
@RequestMapping(value = "/experts", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Experts")
public class ExpertController {

    private final ExpertService expertService;
    private final ExpertDetailService expertDetailService;
    private final AppointmentService appointmentService;

    @GetMapping
    @Operation(
            summary = "List experts",
            description = "Returns experts for the listing page with search, filters and pagination. " +
                    "List parameters like specializationIds/languageCodes accept comma-separated values (e.g. specializationIds=1,2)."
    )
    public PageResponse<ExpertCardDto> listExperts(
            @Parameter(description = "Search by expert name/title or specialization name")
            @RequestParam(required = false) String q,

            @Parameter(description = "Filter by specialization IDs (comma-separated)")
            @RequestParam(required = false) List<Integer> specializationIds,

            @Parameter(description = "Filter by language codes (comma-separated, e.g. en,vi)")
            @RequestParam(required = false) List<String> languageCodes,

            @Parameter(description = "Filter by gender (female|male|other)")
            @RequestParam(required = false) String gender,

            @Parameter(description = "Filter by verified status")
            @RequestParam(required = false) Boolean verified,

            @Parameter(description = "Filter by minimum hourly rate")
            @RequestParam(required = false) BigDecimal minRate,

            @Parameter(description = "Filter by maximum hourly rate")
            @RequestParam(required = false) BigDecimal maxRate,

            @Parameter(description = "Filter by having an unbooked availability slot starting from this datetime (ISO-8601)")
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime availableFrom,

            @Parameter(description = "Filter by having an unbooked availability slot up to this datetime (ISO-8601)")
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime availableTo,

            @ParameterObject
            @PageableDefault(size = 12) Pageable pageable
    ) {
        return expertService.listExperts(
                new ExpertSearchCriteria(q, specializationIds, languageCodes, gender, verified, minRate, maxRate, availableFrom, availableTo),
                pageable
        );
    }

    @GetMapping("/filters")
    @Operation(summary = "Get expert filter options", description = "Returns reference data for expert filters (languages, specializations, genders).")
    public ExpertFilterOptionsDto getFilterOptions() {
        return expertService.getFilterOptions();
    }

    @GetMapping("/{expertId}")
    @Operation(summary = "Get expert detail", description = "Returns data needed for the expert detail page (basic info + languages + specializations + rating stats).")
    public ExpertDetailDto getExpertDetail(
            @Parameter(example = "1")
            @PathVariable Integer expertId
    ) {
        return expertDetailService.getExpertDetail(expertId);
    }

    @GetMapping("/{expertId}/availability")
    @Operation(
            summary = "Get expert availability",
            description = "Returns available (unbooked) time slots for an expert within a datetime range. If from/to are omitted, defaults to [now, now+14 days]."
    )
    public List<AvailabilitySlotDto> getAvailability(
            @PathVariable Integer expertId,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime to
    ) {
        return expertDetailService.getAvailability(expertId, from, to);
    }

    @GetMapping("/{expertId}/reviews")
    @Operation(summary = "List expert reviews", description = "Returns reviews for an expert with pagination.")
    public PageResponse<ExpertReviewDto> getReviews(
            @PathVariable Integer expertId,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable
    ) {
        return expertDetailService.getReviews(expertId, pageable);
    }

    @PostMapping(value = "/{expertId}/appointments", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Book an appointment",
                        description = "Creates a draft appointment using a selected availability slot. Requires authentication.",
                        security = @SecurityRequirement(name = "bearerAuth")
    )
    public CreateAppointmentResponse bookAppointment(
            @PathVariable Integer expertId,
            @AuthenticationPrincipal com.mindwell.be.service.security.UserPrincipal principal,
            @Valid @RequestBody CreateAppointmentDraftRequest req
    ) {
                if (principal == null || principal.getUser() == null || principal.getUser().getUserId() == null) {
                        throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
                }

                Integer userId = principal.getUser().getUserId();
                return appointmentService.createAppointmentDraft(expertId, userId, req.availabilityId());
    }
}
