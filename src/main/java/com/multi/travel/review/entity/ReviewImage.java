package com.multi.travel.review.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="tb_rev_img")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 서버에 저장된 파일명 (UUID 기반)
    private String storedName;

    // 사용자가 업로드한 원본 파일명
    private String originalName;

    // 웹에서 접근 가능한 URL 경로
    private String imageUrl;

    // 업로드 시각
    private LocalDateTime createdAt;

    // 리뷰 연관 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    // 생성 시 자동 시간 설정
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


}
