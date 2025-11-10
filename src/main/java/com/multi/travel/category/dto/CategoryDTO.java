package com.multi.travel.category.dto;

/*
 * Please explain the class!!!
 *
 * @filename    : CategoryDTO
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 10. 월요일
 */

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class CategoryDTO {
    private String cateCode;
    private String cateName;
}
