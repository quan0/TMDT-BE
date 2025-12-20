package com.mindwell.be.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertLanguageId implements Serializable {
    private Integer expertId;
    private String langCode;
}
