package com.multi.travel.review.entity;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : Review
 * @since : 2025. 11. 8. 토요일
 */

import com.multi.travel.plan.entity.TripPlan;
import com.multi.travel.member.entity.Member;
import com.multi.travel.tourspot.entity.TourSpot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 대상 관광지 (옵션: 여행계획에도 연결 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_spot_id")
    private TourSpot tourSpot;

    // (선택) 여행계획에 대한 리뷰라면 TripPlan 참조 가능
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id")
    private TripPlan tripPlan;

    // 리뷰 내용
    @Column(nullable = false, length = 1000)
    private String content;

    // 평점 (1~5)
    @Column(nullable = false)
    private int rating;

    // 작성일자
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

