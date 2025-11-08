package com.multi.travel.course.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Please explain the class!!!
 *
 * @author : lsa03
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

    /** 장소 구분 (TOUR_SPOT / ACCOMMODATION) */
    @Column(name = "place_type", length = 50)
    private String placeType;

    /** 장소 아이디 (관광지 or 숙소) */
    @Column(name = "place_id")
    private Long placeId;

    /** 순서 */
    @Column(name = "order_no")
    private Integer orderNo;

}
