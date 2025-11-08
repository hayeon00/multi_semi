package com.multi.travel.mock.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Please explain the class!!!
 *
 * @author : lsa03
 * @filename : TourSpot
 * @since : 2025-11-08 토요일
 */

@Entity
@Table(name = "mock_tb_tsp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockTourSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spot_id")
    private Long spotId;

    @Column(name = "title")
    private String title;

    @Column(name = "mapx")
    private Double mapx;

    @Column(name = "mapy")
    private Double mapy;

    @Column(name = "first_image")
    private String firstImage;
}
