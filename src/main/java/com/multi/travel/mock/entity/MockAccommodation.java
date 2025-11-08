package com.multi.travel.mock.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 임시 숙소 엔티티
 *
 * @author : lsa03
 * @filename : Accommodation
 * @since : 2025-11-08 토요일
 */
@Entity
@Table(name = "mock_tb_acc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockAccommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "acc_id")
    private Long accId;

    @Column(name = "title")
    private String title;

    @Column(name = "mapx")
    private Double mapx;

    @Column(name = "mapy")
    private Double mapy;

    @Column(name = "first_image")
    private String firstImage;
}
