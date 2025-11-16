package com.multi.travel.review.entity;

import com.multi.travel.member.entity.Member;
import com.multi.travel.plan.entity.TripPlan;
import com.multi.travel.review.dto.MainReviewDto;
import com.multi.travel.review.dto.SpotReviewDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="tb_rev")
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

    private String targetType;

    private Long targetId;

    private LocalDateTime createdAt;


    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewImage> images = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void addImage(ReviewImage image) {
        image.setReview(this);
        this.images.add(image);
    }


    public void update(MainReviewDto mainReview) {
            this.title = mainReview.getTitle();
            this.content = mainReview.getContent();
            this.rating = mainReview.getRating();

    }

    public void update(SpotReviewDto spotReview) {

            this.content = spotReview.getContent();
            this.rating = spotReview.getRating();

    }
}