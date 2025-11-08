package com.multi.travel.plan.entity;

import com.multi.travel.course.entity.Course;
import com.multi.travel.member.entity.Member;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 사용자의 여행 계획
 * 여러 Plan이 하나의 Course를 공유할 수 있음 (N:1)
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

//    @OneToMany(mappedBy = "tripPlan", cascade = CascadeType.ALL)
//    private List<Course> courses = new ArrayList<>();

    /** 여러 Plan이 하나의 Course를 참조 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

}
