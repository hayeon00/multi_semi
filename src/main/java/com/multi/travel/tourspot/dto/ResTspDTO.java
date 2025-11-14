package com.multi.travel.tourspot.dto;

/*
 * Please explain the class!!!
 *
 * @filename    : ResSimpleTspDTO
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 11. 화요일
 */

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@Builder
@ToString
public class ResTspDTO {
    private Long id;
    private String address;
    private String title;
    private Integer recCount;
    private String firstImage;
    private BigDecimal mapx;
    private BigDecimal mapy;
}
