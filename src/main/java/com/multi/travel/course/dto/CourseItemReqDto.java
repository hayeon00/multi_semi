package com.multi.travel.course.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 코스에 들어갈 항목 오청 dto
 *
 * @author : seunga03
 * @filename : CourseItemReqDto
 * @since : 2025-11-08 토요일
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseItemReqDto {
//    private Long courseId;
    @NotBlank(message = "카테고리 코드는 필수입니다.")
    private String categoryCode; // 기존의 placeType을 카테고리 코드로 변경

    @NotNull(message = "placeId는 필수입니다.")
    private Long placeId; // 관광지/숙소의 ID

    private String placeName;

    @NotNull(message = "orderNo는 필수입니다.")
    private Integer orderNo; // 순서

    @NotNull(message = "dayNo는 필수입니다.")
    private Integer dayNo;  // 몇 일차에 속하는지


}
