package com.mindwell.be.dto.blog;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "BlogCategory")
public record BlogCategoryDto(
        Integer categoryId,
        String name
) {
}
