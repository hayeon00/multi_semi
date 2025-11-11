package com.multi.travel.recommend.dto;

/*
 * Please explain the class!!!
 *
 * @filename    : RecDTO
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 11. 화요일
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecDTO {
    private Long id;
    private Long targetId;
    private String status;
    private String catCode;
    private Long userId;
}
