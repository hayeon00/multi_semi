package com.multi.travel.mock.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 임시 plan 엔티티
 *
 * @author : lsa03
 * @filename : Plan
 * @since : 2025-11-08 토요일
 */
@Entity
@Table(name = "tb_pln")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "plan_title")
    private String planTitle;

}