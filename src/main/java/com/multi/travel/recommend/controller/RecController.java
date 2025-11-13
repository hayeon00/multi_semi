package com.multi.travel.recommend.controller;

/*
 * Please explain the class!!!
 *
 * @filename    : RecController
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 11. 화요일
 */


import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.common.ResponseDto;
import com.multi.travel.recommend.service.RecService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/recommend")
public class RecController {

    private final RecService recService;

    @GetMapping("/toggle")
    public ResponseEntity<ResponseDto> toggleRecommend(
            @RequestParam @Valid Long targetId,
            @RequestParam @Valid String catCode,
            @AuthenticationPrincipal CustomUser customUser
            ) {

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "추천 값 변경 성공", recService.toggleRecommend(customUser, targetId, catCode)));
    }
}
