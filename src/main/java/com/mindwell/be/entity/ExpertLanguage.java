package com.mindwell.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "expert_languages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertLanguage {
    @EmbeddedId
    private ExpertLanguageId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("expertId")
    @JoinColumn(name = "expert_id")
    private Expert expert;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("langCode")
    @JoinColumn(name = "lang_code")
    private Language language;
}
