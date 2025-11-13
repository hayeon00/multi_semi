package com.multi.travel.course.entity;

import com.multi.travel.category.entity.Category;
import jakarta.persistence.*;
import lombok.*;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : CourseItem
 * @since : 2025-11-08 토요일
 */
@Entity
@Table(name = "tb_crs_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    /** 코스 참조 (N:1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /** 장소 카테고리 (예: TOUR_SPOT, ACCOMMODATION 등) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cat_code", nullable = false)
    private Category category;

    /** 장소 ID (TourSpot.id 또는 Acc.id) */
    @Column(name = "place_id", nullable = false)
    private Long placeId;

    /** n일차 정보 */
    @Column(name = "day_no", nullable = false)
    private Integer dayNo;

    /** 순서 (사용자 드래그 순서 기준) */
    @Column(name = "order_no", nullable = false)
    private Integer orderNo;



}
