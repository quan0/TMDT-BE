package com.mindwell.be.dto.blog;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "BlogPostCard")
public record BlogPostCardDto(
        Integer postId,
        String slug,
        String title,
        String excerpt,
        String coverImageUrl,
        LocalDateTime publishedAt,
        Integer readingMinutes,
        BlogAuthorDto author,
        List<BlogCategoryDto> categories
) {
}
