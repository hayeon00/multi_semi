package com.multi.travel.review.dto;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : ComplexReviewReqDto
 * @since : 2025. 11. 14. 금요일
 */

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
@Data
public class ComplexReviewReqDto {

    @Valid
    @NotNull
    private MainReviewDto mainReview;
    private List<SpotReviewDto> spotReviews;
    private List<String> deletedImageUrls;
}