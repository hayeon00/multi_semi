package com.multi.travel.attraction.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : Attraction
 * @since : 2025. 11. 8. 토요일
 */
@Entity
public class Attraction {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String location;
    private String description;

    // 여행 계획 생성시 출발위치 얻어옴
    public String getLocation() {
        return this.location;
    }
}
