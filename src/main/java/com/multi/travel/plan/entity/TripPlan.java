package com.multi.travel.plan.entity;

import com.multi.travel.member.entity.Member;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : TripPlan
 * @since : 2025. 11. 8. 토요일
 */
@Entity
public class TripPlan {
    @Id
    @GeneratedValue
    private Long id;

    private String title;
    private String startLocation;
    private int numberOfPeople;

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne
    private Member member;

    @OneToMany(mappedBy = "tripPlan", cascade = CascadeType.ALL)
    private List<Course> courses = new ArrayList<>();
}
