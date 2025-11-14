package com.multi.travel.tourspot.controller;

/*
 * Please explain the class!!!
 *
 * @filename    : TspController
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 9. 일요일
 */

import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.common.ResponseDto;
import com.multi.travel.tourspot.service.TspService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/spots")
public class TspController {

    private final TspService tspService;


    @GetMapping("/list")
    public ResponseEntity<ResponseDto> getTspList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        Map<String, Object> result;
        String message;

        if (keyword.isBlank()) {
            result = tspService.getTourSpotList(page, size, sort, customUser);
            message = "관광지 목록 조회 성공";
        } else {
            result = tspService.getTspSearch(page, size, sort, keyword, customUser);
            message = "관광지 검색 성공";
        }
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, message, result));
    }


    @GetMapping("/detail")
    public ResponseEntity<ResponseDto> getTspDetail(
            @RequestParam @Valid Long id,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "상세 조회 성공", tspService.getTourSpotDetail(id, customUser)));
    }

    @GetMapping("/distance")
    public ResponseEntity<ResponseDto> getTspSortByDistance(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam Long id) {

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "success", tspService.getTspSortByDistance(page, size, id)));
    }

    /** 지도용 관광지 목록 */
    @GetMapping("/simple")
    public ResponseEntity<ResponseDto> getTspSimpleList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "관광지 간단 목록 조회 성공", tspService.getTspSimpleList(page, size))
        );
    }

}
