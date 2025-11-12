package com.multi.travel.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AICourseFeedbackReqDto
 * @since : 2025-11-10 월요일
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AICourseFeedbackReqDto {
    private Long planId;
    private String feedback;
}
