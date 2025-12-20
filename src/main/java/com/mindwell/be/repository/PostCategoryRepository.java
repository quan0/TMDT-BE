package com.mindwell.be.repository;

import com.mindwell.be.entity.PostCategory;
import com.mindwell.be.entity.PostCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCategoryRepository extends JpaRepository<PostCategory, PostCategoryId> {
}
