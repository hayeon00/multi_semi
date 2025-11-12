package com.multi.travel.tourspot.dto;

/*
 * Please explain the class!!!
 *
 * @filename    : ResDistanceTspDTO
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 11. 화요일
 */

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class ResDistanceTspDTO {
    private Long id;
    private String address;
    private String title;
    private Integer recCount;
    private String firstImage;
    private Double distanceMeter;
}
