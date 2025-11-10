package com.multi.travel.review.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ReviewImage
 * @since : 2025. 11. 9. 일요일
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

}
