package com.mindwell.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "expert_specializations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertSpecialization {
    @EmbeddedId
    private ExpertSpecializationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("expertId")
    @JoinColumn(name = "expert_id")
    private Expert expert;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("specId")
    @JoinColumn(name = "spec_id")
    private Specialization specialization;
}
