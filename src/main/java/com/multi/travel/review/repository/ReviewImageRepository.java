package com.multi.travel.review.repository;

import com.multi.travel.review.entity.Review;
import com.multi.travel.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : ReviewImageRepository
 * @since : 2025. 11. 12. 수요일
 */

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    void deleteByReview(Review review);
}
