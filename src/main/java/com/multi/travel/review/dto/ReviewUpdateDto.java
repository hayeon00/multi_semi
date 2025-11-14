package com.multi.travel.review.dto;

import lombok.Data;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ReviewUpdateDto
 * @since : 2025. 11. 14. 금요일
 */
@Data
public class ReviewUpdateDto {
    private String title;
    private String content;
    private int rating;
    // 이미지 수정이 필요하다면 MultipartFile[] images;
}