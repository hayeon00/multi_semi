package com.multi.travel.review.repository;

import com.multi.travel.member.entity.Member;
import com.multi.travel.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : ReviewRepository
 * @since : 2025. 11. 9. 일요일
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTargetTypeAndTargetId(String targetType, Long targetId);

    Page<Review> findAllByTargetTypeIn(List<String> validTypes, Pageable pageable);

    List<Review> findByMember(Member member);

}
