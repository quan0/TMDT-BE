package com.mindwell.be.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(name = "PageResponse")
public record PageResponse<T>(
        @Schema(description = "Items for the current page")
        List<T> items,
        @Schema(example = "0", description = "0-based page index")
        int page,
        @Schema(example = "12")
        int size,
        @Schema(example = "200")
        long totalItems,
        @Schema(example = "17")
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
