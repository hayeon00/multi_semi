package com.multi.travel.admin.dto;

import lombok.*;

/**
 * Please explain the class!!!
 *
 * @author : chang
 * @filename : TourSpotSimpleResDto
 * @since : 2025-11-13 목요일
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourSpotSimpleResDto {
    private Long id;
    private String title;
    private String address;
    private String tel;
    private String status;  // "Y" / "N"

    private String firstImage;

    private Integer recCount;
}