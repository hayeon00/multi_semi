package com.multi.travel.plan.entity;

import com.multi.travel.course.entity.Course;
import com.multi.travel.member.entity.Member;
import com.multi.travel.review.entity.Review;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 여행 기본 계획 엔티티
 * - 출발 위치는 관광지(TourSpot)에서 가져옴
 * - AI 추천 기반 계획 여부(isAiPlan)
 * - Course(코스)와 다대일 관계
 *
 * @author : rlagkdus
 * @since : 2025. 11. 8
 */

@Entity
@Table(name="trip_plan")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long id;

    private String title;

    private String startLocation;

    @Column(name = "start_mapx", precision = 13, scale = 10)
    private BigDecimal startMapX;

    @Column(name = "start_mapy", precision = 13, scale = 10)
    private BigDecimal startMapY;

    private boolean isAiPlan;

    private char status;

    private int numberOfPeople;

    private LocalDate startDate;

    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "tour_spot_id")
    private Long tourSpotId;

    @OneToMany(mappedBy = "tripPlan", cascade = CascadeType.ALL, orphanRemoval = true) // ⭐ 이 부분이 핵심!
    private List<Review> reviews;


    public void update(String title, int numberOfPeople, LocalDate startDate, LocalDate endDate,
                       String startLocation, BigDecimal startMapX, BigDecimal startMapY, Long tourSpotId) {
        this.title = title;
        this.numberOfPeople = numberOfPeople;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startLocation = startLocation;
        this.startMapX = startMapX;
        this.startMapY = startMapY;
        this.tourSpotId = tourSpotId;   // 추가
    }







}
