package com.mindwell.be.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertSpecializationId implements Serializable {
    private Integer expertId;
    private Integer specId;
}
