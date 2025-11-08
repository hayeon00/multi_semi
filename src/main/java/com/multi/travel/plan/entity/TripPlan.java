package com.multi.travel.plan.entity;

import com.multi.travel.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 여행 제목
    @Column(nullable = false)
    private String title;

    // 출발 위치
    @Column(nullable = false)
    private String startLocation;

    // 인원 수
    @Column(nullable = false)
    private int numberOfPeople;

    // 여행 시작일
    @Column(nullable = false)
    private LocalDate startDate;

    // 여행 종료일
    @Column(nullable = false)
    private LocalDate endDate;

    // 여행 계획 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 연결된 코스들
    @OneToMany(mappedBy = "tripPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course> courses = new ArrayList<>();
}

