package com.mindwell.be.repository;

import com.mindwell.be.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

	interface ExpertRatingStatsRow {
		Integer getExpertId();

		Double getAvgRating();

		Long getReviewCount();
	}

	@Query("""
			select r.expert.expertId as expertId,
				   avg(r.rating) as avgRating,
				   count(r.reviewId) as reviewCount
			from Review r
			where r.expert.expertId in :expertIds
			group by r.expert.expertId
			""")
	List<ExpertRatingStatsRow> findRatingStatsByExpertIds(@Param("expertIds") List<Integer> expertIds);

	    @Query("""
		    select r.expert.expertId as expertId,
			   avg(r.rating) as avgRating,
			   count(r.reviewId) as reviewCount
		    from Review r
		    where r.expert.expertId = :expertId
		    group by r.expert.expertId
		    """)
	    ExpertRatingStatsRow findRatingStatsByExpertId(@Param("expertId") Integer expertId);

	    interface ExpertReviewRow {
		Integer getReviewId();

		Integer getRating();

		String getComment();

		Integer getUserId();

		String getUserFullName();

		java.time.LocalDateTime getAppointmentStartTime();
	    }

	    @Query("""
		    select r.reviewId as reviewId,
			   r.rating as rating,
			   r.comment as comment,
			   u.userId as userId,
			   u.fullName as userFullName,
			   a.startTime as appointmentStartTime
		    from Review r
		    left join r.user u
		    left join r.appointment a
		    where r.expert.expertId = :expertId
		    order by r.reviewId desc
		    """)
	    Page<ExpertReviewRow> findReviewRowsByExpertId(@Param("expertId") Integer expertId, Pageable pageable);
}
