package com.multi.travel.admin.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Please explain the class!!!
 *
 * @author : chang
 * @filename : TourSpotResDto
 * @since : 2025-11-11 화요일
 */
@Data
@Builder
@ToString
public class TourSpotResDto {

    private Long id;
    private String title;
    private String description;
    private String address;
    private String tel;
    private String homepage;
    private Integer areacode;
    private Integer sigungucode;
    private String lDongRegnCd;
    private Integer recCount;
    private String status;
    private BigDecimal mapx;
    private BigDecimal mapy;
    private String firstImage;  // 이미지
}
