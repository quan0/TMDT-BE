package com.mindwell.be.dto.blog;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "BlogPostDetail")
public record BlogPostDetailDto(
        Integer postId,
        String slug,
        String title,
        @Schema(
                description = "Long-form article content. Interpret this value based on contentFormat (markdown/html/plaintext).",
                example = """
                        Trong cuộc sống hiện đại đầy áp lực, nhiều người trong chúng ta có thể gặp phải các vấn đề về sức khỏe tinh thần mà không nhận ra.

                        ## 1. Thay đổi trong cảm xúc
                        Cảm giác buồn bã kéo dài, mất hứng thú với những hoạt động yêu thích...

                        ## 2. Thay đổi thói quen ngủ
                        Mất ngủ kéo dài, ngủ quá nhiều...

                        > Giấc ngủ là nền tảng của sức khỏe tinh thần. Khi giấc ngủ bị ảnh hưởng, toàn bộ sức khỏe tâm lý của chúng ta có thể bị lung lay.

                        ## 3. Thay đổi về thể chất
                        Đau đầu thường xuyên, đau bụng...
                        """
        )
        String content,
        @Schema(description = "Content serialization format for the `content` field", allowableValues = {"markdown", "html", "plaintext"}, example = "markdown")
        BlogContentFormat contentFormat,
        String coverImageUrl,
        LocalDateTime publishedAt,
        Integer readingMinutes,
        BlogAuthorDto author,
        List<BlogCategoryDto> categories
) {
}
