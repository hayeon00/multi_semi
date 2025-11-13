package com.multi.travel.tourspot.dto;

/*
 * Please explain the class!!!
 *
 * @filename    : TourSpotSimpleProjection
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 11. 화요일
 */


public interface TspHasDistanceProjection {
    Long getId();

    String getTitle();

    String getAddress();

    Integer getRecCount();

    String getFirstImage();

    Double getDistanceKm();
}
