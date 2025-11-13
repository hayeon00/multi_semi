package com.multi.travel.acc.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : ResAccSimpleDTO
 * @since : 2025-11-13 목요일
 */

@Builder
@Data
@ToString
public class ResAccSimpleDTO {
    private Long id;
    private String title;
//    private String address;
    private BigDecimal mapx;
    private BigDecimal mapy;
//    private String firstImage;
}
