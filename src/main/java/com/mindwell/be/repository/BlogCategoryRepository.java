package com.mindwell.be.repository;

import com.mindwell.be.entity.BlogCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Integer> {

	List<BlogCategory> findAllByOrderByNameAsc();
}
