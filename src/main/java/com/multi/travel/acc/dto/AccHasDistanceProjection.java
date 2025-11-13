package com.multi.travel.acc.dto;

/*
 * Please explain the class!!!
 *
 * @filename    : AccHasDistanceProjection
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 11. 화요일
 */


import java.math.BigDecimal;

public interface AccHasDistanceProjection {
    Long getId();

    String getTitle();

    String getAddress();

    Integer getRecCount();

    String getFirstImage();

    BigDecimal getDistanceKm();
}
