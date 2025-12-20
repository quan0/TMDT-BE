package com.mindwell.be.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCategoryId implements Serializable {
    private Integer postId;
    private Integer categoryId;
}
