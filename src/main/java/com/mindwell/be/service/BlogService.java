package com.mindwell.be.service;

import com.mindwell.be.dto.blog.*;
import com.mindwell.be.dto.common.PageResponse;
import com.mindwell.be.entity.BlogCategory;
import com.mindwell.be.entity.BlogPost;
import com.mindwell.be.entity.Expert;
import com.mindwell.be.repository.BlogCategoryRepository;
import com.mindwell.be.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogCategoryRepository blogCategoryRepository;
    private final BlogPostRepository blogPostRepository;

    @Transactional(readOnly = true)
    public List<BlogCategoryDto> listCategories() {
        return blogCategoryRepository.findAllByOrderByNameAsc().stream()
                .map(c -> new BlogCategoryDto(c.getCategoryId(), c.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<BlogPostCardDto> listPosts(String q, Integer categoryId, Pageable pageable) {
        // Default sort: publishedAt desc, fallback postId desc
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Order.desc("publishedAt").nullsLast(), Sort.Order.desc("postId")));
        }

        Page<Integer> idPage = blogPostRepository.searchPublishedIds(q, categoryId, pageable);
        List<Integer> ids = idPage.getContent();
        if (ids.isEmpty()) {
            return new PageResponse<>(
                    List.of(),
                    idPage.getNumber(),
                    idPage.getSize(),
                    idPage.getTotalElements(),
                    idPage.getTotalPages(),
                    idPage.hasNext(),
                    idPage.hasPrevious()
            );
        }

        Map<Integer, Integer> orderIndex = indexById(ids);
        List<BlogPost> posts = blogPostRepository.findByPostIdIn(ids).stream()
                .sorted(Comparator.comparingInt(p -> orderIndex.getOrDefault(p.getPostId(), Integer.MAX_VALUE)))
                .toList();

        return new PageResponse<>(
                posts.stream().map(this::toCardDto).toList(),
                idPage.getNumber(),
                idPage.getSize(),
                idPage.getTotalElements(),
                idPage.getTotalPages(),
                idPage.hasNext(),
                idPage.hasPrevious()
        );
    }

    @Transactional(readOnly = true)
    public BlogPostDetailDto getPost(Integer postId) {
        BlogPost post = blogPostRepository.findByPostId(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        return toDetailDto(post);
    }

    @Transactional(readOnly = true)
    public List<BlogPostCardDto> relatedPosts(Integer postId, int limit) {
        BlogPost post = blogPostRepository.findByPostId(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        List<Integer> categoryIds = post.getCategories().stream()
                .map(BlogCategory::getCategoryId)
                .sorted(Comparator.naturalOrder())
                .toList();

        if (categoryIds.isEmpty()) {
            return List.of();
        }

        Pageable pageable = PageRequest.of(0, Math.max(1, Math.min(limit, 12)));
        List<Integer> ids = blogPostRepository.findRelatedIds(categoryIds, postId, pageable);
        if (ids.isEmpty()) return List.of();

        Map<Integer, Integer> orderIndex = indexById(ids);
        return blogPostRepository.findByPostIdIn(ids).stream()
                .sorted(Comparator.comparingInt(p -> orderIndex.getOrDefault(p.getPostId(), Integer.MAX_VALUE)))
                .map(this::toCardDto)
                .toList();
    }

    private static Map<Integer, Integer> indexById(List<Integer> ids) {
        Map<Integer, Integer> index = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            Integer id = ids.get(i);
            if (id != null && !index.containsKey(id)) {
                index.put(id, i);
            }
        }
        return index;
    }

    private BlogPostCardDto toCardDto(BlogPost post) {
        return new BlogPostCardDto(
                post.getPostId(),
                slug(post),
                post.getTitle(),
                safeExcerpt(post),
                post.getCoverImageUrl(),
                post.getPublishedAt(),
                readingMinutes(post),
                toAuthorDto(post.getAuthor()),
                post.getCategories().stream()
                        .map(c -> new BlogCategoryDto(c.getCategoryId(), c.getName()))
                        .toList()
        );
    }

    private BlogPostDetailDto toDetailDto(BlogPost post) {
        BlogContentFormat format = detectContentFormat(post == null ? null : post.getContent());
        return new BlogPostDetailDto(
                post.getPostId(),
                slug(post),
                post.getTitle(),
                post.getContent(),
                format,
                post.getCoverImageUrl(),
                post.getPublishedAt(),
                readingMinutes(post),
                toAuthorDto(post.getAuthor()),
                post.getCategories().stream()
                        .map(c -> new BlogCategoryDto(c.getCategoryId(), c.getName()))
                        .toList()
        );
    }

    private static BlogContentFormat detectContentFormat(String content) {
        if (content == null || content.isBlank()) return BlogContentFormat.MARKDOWN;
        String c = content.trim();
        // Simple HTML detection (common tags) to avoid FE rendering markdown for legacy HTML content.
        if (c.startsWith("<!DOCTYPE") || c.startsWith("<html") || c.startsWith("<p") || c.startsWith("<h")
                || c.contains("</p>") || c.contains("<br") || c.contains("</h") || c.contains("</div>")
                || c.contains("<ul") || c.contains("<ol") || c.contains("<li")) {
            return BlogContentFormat.HTML;
        }
        // Otherwise default to markdown (FE can render plaintext as markdown safely).
        return BlogContentFormat.MARKDOWN;
    }

    private static BlogAuthorDto toAuthorDto(Expert expert) {
        if (expert == null) return null;
        return new BlogAuthorDto(expert.getExpertId(), expert.getFullName(), expert.getTitle());
    }

    private static Integer readingMinutes(BlogPost post) {
        if (post == null) return null;
        if (post.getReadingMinutes() != null && post.getReadingMinutes() > 0) return post.getReadingMinutes();
        String content = post.getContent();
        if (content == null || content.isBlank()) return 1;
        int words = content.trim().split("\\s+").length;
        // 200 wpm heuristic
        return Math.max(1, (int) Math.ceil(words / 200.0));
    }

    private static String safeExcerpt(BlogPost post) {
        if (post == null) return null;
        if (post.getExcerpt() != null && !post.getExcerpt().isBlank()) return post.getExcerpt();
        String content = post.getContent();
        if (content == null) return null;
        String normalized = content.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 180) return normalized;
        return normalized.substring(0, 177) + "...";
    }

    private static String slug(BlogPost post) {
        String title = post == null ? null : post.getTitle();
        String base = title == null ? "post" : title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        if (base.isBlank()) base = "post";
        return (post == null || post.getPostId() == null) ? base : (post.getPostId() + "-" + base);
    }
}
