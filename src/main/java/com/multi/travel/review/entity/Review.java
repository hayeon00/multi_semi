package com.multi.travel.review.entity;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : Review
 * @since : 2025. 11. 8. 토요일
 */

import com.multi.travel.member.entity.Member;
import com.multi.travel.plan.entity.TripPlan;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
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

    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    private int rating;

    private LocalDateTime createdAt;

    // 이미지 연관관계
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // 추가 이유: Builder로 생성할 때도 초기값이 반영되도록
    private List<ReviewImage> images = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}


