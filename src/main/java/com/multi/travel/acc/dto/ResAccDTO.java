package com.multi.travel.acc.dto;

/*
 * Please explain the class!!!
 *
 * @filename    : ResAccDTO
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
public class ResAccDTO {
    private Long id;
    private String address;
    private String title;
    private Integer recCount;
    private String firstImage;
    private String status;  // 활성화 상태
    private BigDecimal mapx;
    private BigDecimal mapy;
}
