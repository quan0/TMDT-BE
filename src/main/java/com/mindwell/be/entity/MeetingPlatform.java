package com.mindwell.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "meeting_platforms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_meeting_platforms_platform_key", columnNames = {"platform_key"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingPlatform {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer platformId;

    private String platformKey;
    private String displayName;
    private String description;
    private Boolean isActive;
}
