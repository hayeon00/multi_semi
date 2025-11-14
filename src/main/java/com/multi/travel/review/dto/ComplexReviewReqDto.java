package com.multi.travel.review.dto;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ComplexReviewReqDto
 * @since : 2025. 11. 14. 금요일
 */

import lombok.Data;
import java.util.List;
@Data
public class ComplexReviewReqDto {
    private MainReviewDto mainReview;
    private List<SpotReviewDto> spotReviews;
}