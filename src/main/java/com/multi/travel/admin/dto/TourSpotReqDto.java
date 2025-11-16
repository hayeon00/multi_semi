package com.multi.travel.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * Please explain the class!!!
 *
 * @author : chang
 * @filename : TourSpotReqDto
 * @since : 2025-11-11 화요일
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourSpotReqDto {

    private String address;
    private String title;
    private String description;
    private String tel;

    private String homepage;

    private Integer areacode;
    private Integer sigungucode;
    @JsonProperty("LDongRegnCd")  // 🔥 추가!
    private String lDongRegnCd;   // 행정동 코드

    private Integer recCount;

    private BigDecimal mapx;       // 경도
    private BigDecimal mapy;       // 위도

  //  private Long catCode;          // 카테고리 코드 (Category FK 매핑용)

    private MultipartFile firstImageFile;   // 대표 이미지


}
