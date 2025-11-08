package com.multi.travel.plan.entity;

import com.multi.travel.course.entity.Course;
import com.multi.travel.member.entity.Member;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : TripPlan
 * @since : 2025. 11. 8. 토요일
 */
@Entity
public class TripPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long id;

    private String title;
    private String startLocation;
    private int numberOfPeople;

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne
    private Member member;

    /** 여러 Plan이 하나의 Course를 참조 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;



}
