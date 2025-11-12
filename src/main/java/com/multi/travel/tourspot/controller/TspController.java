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
            @AuthenticationPrincipal CustomUser customUser
    ) {

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "success", tspService.getTourSpotList(page, size, sort, customUser)));
    }

    @GetMapping("/detail")
    public ResponseEntity<ResponseDto> getTspDetail(
            @RequestParam @Valid Long id,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "상세 조회 성공", tspService.getTourSpotDetail(id, customUser)));
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseDto> getTspSearch(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "success", tspService.getTspSearch(page, size, sort, keyword, customUser)));
    }

    @GetMapping("/distance")
    public ResponseEntity<ResponseDto> getTspSortByDistance(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam Long id) {

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "success", tspService.getTspSortByDistance(page, size, id)));
    }
}
