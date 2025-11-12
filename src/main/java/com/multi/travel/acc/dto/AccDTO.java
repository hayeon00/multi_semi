package com.multi.travel.acc.dto;

/*
 * Please explain the class!!!
 *
 * @filename    : AccDTO
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 10. 월요일
 */


import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ToString
@Builder
public class AccDTO {
    private Long id;
    private String address;
    private String title;
    private String description;
    private String homepage;
    private String tel;
    private BigDecimal mapx;
    private BigDecimal mapy;
    private Integer areacode;
    private Integer sigungucode;
    private String lDongRegnCd;
    private String firstImage;  // 1번 이미지
    private String firstImage2; // 2번 이미지
    private LocalDateTime createdAt;  // 생성일자
    private LocalDateTime modifiedAt; // 수정일자
    private String status;  // 활성화 상태
    private Integer recCount;  // 추천 개수
    private Integer contentId;
    private String catCode;
    private MultipartFile imageFile;
}
