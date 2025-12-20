package com.mindwell.be.service;

import com.mindwell.be.dto.common.PageResponse;
import com.mindwell.be.dto.expert.*;
import com.mindwell.be.entity.Expert;
import com.mindwell.be.entity.ExpertAvailability;
import com.mindwell.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExpertDetailService {
    private final ExpertRepository expertRepository;
    private final ExpertLanguageRepository expertLanguageRepository;
    private final ExpertSpecializationRepository expertSpecializationRepository;
    private final ExpertAvailabilityRepository expertAvailabilityRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public ExpertDetailDto getExpertDetail(Integer expertId) {
        Expert expert = expertRepository.findById(expertId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expert not found"));

        List<LanguageOptionDto> languages = expertLanguageRepository.findLanguageRowsByExpertIds(List.of(expertId))
                .stream()
                .map(r -> new LanguageOptionDto(r.getLangCode(), r.getName()))
                .toList();

        List<SpecializationOptionDto> specs = expertSpecializationRepository.findSpecializationRowsByExpertIds(List.of(expertId))
                .stream()
                .map(r -> new SpecializationOptionDto(r.getSpecId(), r.getName()))
                .toList();

        ReviewRepository.ExpertRatingStatsRow stats = reviewRepository.findRatingStatsByExpertId(expertId);
        Double avgRating = stats == null ? 0.0 : stats.getAvgRating();
        if (avgRating != null) {
            avgRating = Math.round(avgRating * 10.0) / 10.0;
        }
        Long reviewCount = stats == null ? 0L : stats.getReviewCount();

        return new ExpertDetailDto(
                expert.getExpertId(),
                expert.getFullName(),
                expert.getTitle(),
                expert.getHourlyRate(),
                expert.getIsVerified(),
                expert.getGender(),
                avgRating,
                reviewCount,
                languages,
                specs
        );
    }

    @Transactional(readOnly = true)
    public List<AvailabilitySlotDto> getAvailability(Integer expertId, LocalDateTime from, LocalDateTime to) {
        if (from == null) {
            from = LocalDateTime.now();
        }
        if (to == null) {
            to = from.plusDays(14);
        }
        if (to.isBefore(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "availableTo must be after availableFrom");
        }

        // Ensure expert exists
        if (!expertRepository.existsById(expertId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Expert not found");
        }

        List<ExpertAvailability> slots = expertAvailabilityRepository
                .findByExpert_ExpertIdAndIsBookedFalseAndStartTimeBetweenOrderByStartTimeAsc(expertId, from, to);

        return slots.stream()
                .map(s -> new AvailabilitySlotDto(s.getAvailabilityId(), s.getStartTime(), s.getEndTime()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpertReviewDto> getReviews(Integer expertId, Pageable pageable) {
        if (!expertRepository.existsById(expertId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Expert not found");
        }

        var page = reviewRepository.findReviewRowsByExpertId(expertId, pageable)
                .map(r -> new ExpertReviewDto(
                        r.getReviewId(),
                        r.getRating(),
                        r.getComment(),
                        r.getUserId(),
                        r.getUserFullName(),
                        r.getAppointmentStartTime()
                ));

        return PageResponse.from(page);
    }
}
