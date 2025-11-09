package com.multi.travel.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ReviewResDto
 * @since : 2025. 11. 8. 토요일
 */
@Data
@Builder
@AllArgsConstructor
public class ReviewResDto {
    private String message;
    private List<String> imageUrls;
}