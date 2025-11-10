package com.multi.travel.review.repository;

import com.multi.travel.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : ReviewRepository
 * @since : 2025. 11. 9. 일요일
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

}
