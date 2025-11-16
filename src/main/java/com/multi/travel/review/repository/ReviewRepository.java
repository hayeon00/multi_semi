package com.multi.travel.review.repository;

import com.multi.travel.member.entity.Member;
import com.multi.travel.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    Page<Review> findByTargetTypeAndTargetId(String targetType, Long targetId, Pageable pageable);
    Page<Review> findByMember(Member member, Pageable pageable);
    List<Review> findAllByTargetTypeAndTargetId(String place, Long placeId);


    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.member m " +        // Member 엔티티 즉시 로드
            "JOIN FETCH r.tripPlan tp " +     // TripPlan 엔티티 즉시 로드
            "LEFT JOIN FETCH r.images i " +   // ReviewImage 엔티티 즉시 로드 (이미지가 없을 수도 있으므로 LEFT JOIN)
            "WHERE r.tripPlan.id = :planId AND m.id = :memberId")
    List<Review> findAllByTripPlan_IdAndMember_Id(Long planId, Long memberId);
}
