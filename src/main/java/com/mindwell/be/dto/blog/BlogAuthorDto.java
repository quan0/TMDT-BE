package com.mindwell.be.dto.blog;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "BlogAuthor")
public record BlogAuthorDto(
        Integer expertId,
        String fullName,
        String title
) {
}
