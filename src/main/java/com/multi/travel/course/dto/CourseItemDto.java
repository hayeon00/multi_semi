package com.multi.travel.course.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : CourseItemDto
 * @since : 2025. 11. 14. 금요일
 */
@Data
@Builder
public class CourseItemDto {
    private Long spotId;       // 관광지 ID
    private String name;       // 관광지 이름
    private String address;    // 주소
    private String imageUrl;   // 장소 이미지 주소
    private String category;   // (선택) 카테고리
}