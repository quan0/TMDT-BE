package com.mindwell.be.controller;

import com.mindwell.be.dto.blog.BlogCategoryDto;
import com.mindwell.be.dto.blog.BlogPostCardDto;
import com.mindwell.be.dto.blog.BlogPostDetailDto;
import com.mindwell.be.dto.common.PageResponse;
import com.mindwell.be.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/blog", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Blog")
public class BlogController {

    private final BlogService blogService;

    @GetMapping("/categories")
    @Operation(
            summary = "List blog categories",
            description = "Returns categories used by the blog listing filter chips."
    )
    public List<BlogCategoryDto> categories() {
        return blogService.listCategories();
    }

    @GetMapping("/posts")
    @Operation(
            summary = "List blog posts",
            description = "Returns blog posts for the listing page with optional search and category filtering."
    )
    public PageResponse<BlogPostCardDto> listPosts(
            @Parameter(description = "Search by title/content")
            @RequestParam(required = false) String q,

            @Parameter(description = "Filter by categoryId")
            @RequestParam(required = false) Integer categoryId,

            @ParameterObject
            @PageableDefault(size = 9) Pageable pageable
    ) {
        return blogService.listPosts(q, categoryId, pageable);
    }

    @GetMapping("/posts/{postId}")
    @Operation(
            summary = "Get blog post detail",
            description = "Returns the post content and metadata for the blog detail page."
    )
    public BlogPostDetailDto postDetail(@PathVariable Integer postId) {
        return blogService.getPost(postId);
    }

    @GetMapping("/posts/{postId}/related")
    @Operation(
            summary = "List related posts",
            description = "Returns related posts for the blog detail page (typically 3 items)."
    )
    public List<BlogPostCardDto> related(
            @PathVariable Integer postId,
            @Parameter(description = "Max number of items", example = "3")
            @RequestParam(defaultValue = "3") int limit
    ) {
        return blogService.relatedPosts(postId, limit);
    }
}
