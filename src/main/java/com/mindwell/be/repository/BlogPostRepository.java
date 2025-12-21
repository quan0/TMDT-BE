package com.mindwell.be.repository;

import com.mindwell.be.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Integer> {

    @Query("""
	    select p.postId
	    from BlogPost p
	    where (:q is null or :q = ''
		   or lower(p.title) like lower(concat('%', :q, '%'))
		   or lower(p.content) like lower(concat('%', :q, '%')))
	      and (:categoryId is null or exists (
			select 1 from p.categories c where c.categoryId = :categoryId
	      ))
	      and (p.isPublished = true or p.isPublished is null)
	    """)
    Page<Integer> searchPublishedIds(@Param("q") String q, @Param("categoryId") Integer categoryId, Pageable pageable);

	@EntityGraph(attributePaths = {"author", "categories"})
	List<BlogPost> findByPostIdIn(List<Integer> postIds);

	@EntityGraph(attributePaths = {"author", "categories"})
	Optional<BlogPost> findByPostId(Integer postId);

    @Query("""
	    select p.postId
	    from BlogPost p
	    where exists (
	      select 1 from p.categories c where c.categoryId in :categoryIds
	    )
	      and p.postId <> :excludePostId
	      and (p.isPublished = true or p.isPublished is null)
	    order by coalesce(p.publishedAt, current_timestamp) desc, p.postId desc
	    """)
	List<Integer> findRelatedIds(@Param("categoryIds") List<Integer> categoryIds, @Param("excludePostId") Integer excludePostId, Pageable pageable);
}
