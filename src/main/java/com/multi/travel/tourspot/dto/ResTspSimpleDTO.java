package com.multi.travel.tourspot.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : ResTspSimpleDTO
 * @since : 2025-11-13 목요일
 */

@Builder
@Data
@ToString
public class ResTspSimpleDTO {
    private Long id;
    private String title;
    private BigDecimal mapx;
    private BigDecimal mapy;
}
