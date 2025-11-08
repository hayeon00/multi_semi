package com.multi.travel.plan.entity;

import com.multi.travel.attraction.entity.Attraction;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : Course
 * @since : 2025. 11. 8. 토요일
 */
@Entity
public class Course {
    @Id
    @GeneratedValue
    private Long id;

    private int dayOrder;
    private int sequence;

    @ManyToOne
    private TripPlan tripPlan;
    @ManyToOne
    private Attraction attraction;
}
