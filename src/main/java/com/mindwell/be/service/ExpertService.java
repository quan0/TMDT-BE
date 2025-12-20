package com.mindwell.be.service;

import com.mindwell.be.dto.common.PageResponse;
import com.mindwell.be.dto.expert.*;
import com.mindwell.be.entity.Expert;
import com.mindwell.be.repository.*;
import com.mindwell.be.service.spec.ExpertSearchCriteria;
import com.mindwell.be.service.spec.ExpertSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpertService {
    private final ExpertRepository expertRepository;
    private final ExpertLanguageRepository expertLanguageRepository;
    private final ExpertSpecializationRepository expertSpecializationRepository;
    private final ReviewRepository reviewRepository;
    private final LanguageRepository languageRepository;
    private final SpecializationRepository specializationRepository;

    public PageResponse<ExpertCardDto> listExperts(ExpertSearchCriteria criteria, Pageable pageable) {
        Page<Expert> page = expertRepository.findAll(ExpertSpecifications.byCriteria(criteria), pageable);

        List<Integer> expertIds = page.getContent().stream()
                .map(Expert::getExpertId)
                .filter(Objects::nonNull)
                .toList();

        Map<Integer, List<LanguageOptionDto>> languagesByExpert = new HashMap<>();
        Map<Integer, List<SpecializationOptionDto>> specsByExpert = new HashMap<>();
        Map<Integer, ReviewRepository.ExpertRatingStatsRow> ratingByExpert = new HashMap<>();

        if (!expertIds.isEmpty()) {
            for (ExpertLanguageRepository.ExpertLanguageRow row : expertLanguageRepository.findLanguageRowsByExpertIds(expertIds)) {
                languagesByExpert.computeIfAbsent(row.getExpertId(), k -> new ArrayList<>())
                        .add(new LanguageOptionDto(row.getLangCode(), row.getName()));
            }

            for (ExpertSpecializationRepository.ExpertSpecializationRow row : expertSpecializationRepository.findSpecializationRowsByExpertIds(expertIds)) {
                specsByExpert.computeIfAbsent(row.getExpertId(), k -> new ArrayList<>())
                        .add(new SpecializationOptionDto(row.getSpecId(), row.getName()));
            }

            for (ReviewRepository.ExpertRatingStatsRow row : reviewRepository.findRatingStatsByExpertIds(expertIds)) {
                ratingByExpert.put(row.getExpertId(), row);
            }
        }

        Page<ExpertCardDto> dtoPage = page.map(expert -> {
            ReviewRepository.ExpertRatingStatsRow stats = ratingByExpert.get(expert.getExpertId());

            Double avgRating = stats == null ? 0.0 : stats.getAvgRating();
            if (avgRating != null) {
                avgRating = Math.round(avgRating * 10.0) / 10.0;
            }

            Long reviewCount = stats == null ? 0L : stats.getReviewCount();

            return new ExpertCardDto(
                    expert.getExpertId(),
                    expert.getFullName(),
                    expert.getTitle(),
                    expert.getHourlyRate(),
                    expert.getIsVerified(),
                    expert.getGender(),
                    avgRating,
                    reviewCount,
                    languagesByExpert.getOrDefault(expert.getExpertId(), List.of()),
                    specsByExpert.getOrDefault(expert.getExpertId(), List.of())
            );
        });

        return PageResponse.from(dtoPage);
    }

    public ExpertFilterOptionsDto getFilterOptions() {
        List<LanguageOptionDto> languages = languageRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(l -> new LanguageOptionDto(l.getLangCode(), l.getName()))
                .toList();

        List<SpecializationOptionDto> specializations = specializationRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(s -> new SpecializationOptionDto(s.getSpecId(), s.getName()))
                .toList();

        List<String> genders = List.of("female", "male", "other");

        return new ExpertFilterOptionsDto(languages, specializations, genders);
    }
}
