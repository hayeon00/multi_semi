package com.multi.travel.review.entity;

import com.multi.travel.member.entity.Member;
import com.multi.travel.plan.entity.TripPlan;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="tb_review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id")
    private TripPlan tripPlan;



    // 리뷰 대상 타입: PLAN, COURSE, TOUR_SPOT, ACCOMMODATION
    @Column(nullable = false, length = 30)
    private String targetType;

    // 리뷰 대상 ID
    @Column(nullable = false)
    private Long targetId;

    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    private int rating;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
