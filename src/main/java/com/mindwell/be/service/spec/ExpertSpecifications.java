package com.mindwell.be.service.spec;

import com.mindwell.be.entity.*;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

public final class ExpertSpecifications {
    private ExpertSpecifications() {
    }

    public static Specification<Expert> byCriteria(ExpertSearchCriteria criteria) {
        return (root, query, cb) -> {
            if (criteria == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (criteria.verified() != null) {
                predicates.add(cb.equal(root.get("isVerified"), criteria.verified()));
            }

            if (criteria.gender() != null && !criteria.gender().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("gender")), criteria.gender().trim().toLowerCase()));
            }

            if (criteria.minRate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("hourlyRate"), criteria.minRate()));
            }

            if (criteria.maxRate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("hourlyRate"), criteria.maxRate()));
            }

            if (criteria.specializationIds() != null && !criteria.specializationIds().isEmpty()) {
                var sq = query.subquery(Integer.class);
                Root<ExpertSpecialization> es = sq.from(ExpertSpecialization.class);
                sq.select(cb.literal(1));
                sq.where(
                        cb.equal(es.get("expert").get("expertId"), root.get("expertId")),
                        es.get("specialization").get("specId").in(criteria.specializationIds())
                );
                predicates.add(cb.exists(sq));
            }

            if (criteria.languageCodes() != null && !criteria.languageCodes().isEmpty()) {
                var sq = query.subquery(Integer.class);
                Root<ExpertLanguage> el = sq.from(ExpertLanguage.class);
                sq.select(cb.literal(1));
                sq.where(
                        cb.equal(el.get("expert").get("expertId"), root.get("expertId")),
                        cb.lower(el.get("language").get("langCode")).in(
                                criteria.languageCodes().stream().map(s -> s == null ? null : s.toLowerCase()).toList()
                        )
                );
                predicates.add(cb.exists(sq));
            }

            if (criteria.availableFrom() != null || criteria.availableTo() != null) {
                var sq = query.subquery(Integer.class);
                Root<ExpertAvailability> ea = sq.from(ExpertAvailability.class);

                List<Predicate> sub = new ArrayList<>();
                sub.add(cb.equal(ea.get("expert").get("expertId"), root.get("expertId")));
                sub.add(cb.isFalse(ea.get("isBooked")));

                if (criteria.availableFrom() != null) {
                    sub.add(cb.greaterThanOrEqualTo(ea.get("startTime"), criteria.availableFrom()));
                }
                if (criteria.availableTo() != null) {
                    sub.add(cb.lessThanOrEqualTo(ea.get("startTime"), criteria.availableTo()));
                }

                sq.select(cb.literal(1));
                sq.where(sub.toArray(Predicate[]::new));
                predicates.add(cb.exists(sq));
            }

            if (criteria.q() != null && !criteria.q().isBlank()) {
                String like = "%" + criteria.q().trim().toLowerCase() + "%";

                Predicate byName = cb.like(cb.lower(root.get("fullName")), like);
                Predicate byTitle = cb.like(cb.lower(root.get("title")), like);

                var sq = query.subquery(Integer.class);
                Root<ExpertSpecialization> es = sq.from(ExpertSpecialization.class);
                sq.select(cb.literal(1));
                sq.where(
                        cb.equal(es.get("expert").get("expertId"), root.get("expertId")),
                        cb.like(cb.lower(es.get("specialization").get("name")), like)
                );
                Predicate bySpecializationName = cb.exists(sq);

                predicates.add(cb.or(byName, byTitle, bySpecializationName));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
