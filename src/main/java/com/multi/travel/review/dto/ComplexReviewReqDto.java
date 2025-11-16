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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplexReviewReqDto {

    @Valid
    @NotNull
    private MainReviewDto mainReview;
    private List<SpotReviewDto> spotReviews;
    private List<String> deletedImageUrls;
}