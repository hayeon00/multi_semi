package com.multi.travel.acc.controller;

/*
 * Please explain the class!!!
 *
 * @filename    : AccController
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 9. 일요일
 */

import com.multi.travel.acc.serivce.AccService;
import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.common.ResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/accommodations")
public class AccController {

    private final AccService accService;

    @GetMapping("/list")
    public ResponseEntity<Object> getAccListPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "areacode") String sort,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        Map<String, Object> result;
        String message;
        if (keyword.isBlank()) {
            result = accService.getAccList(page, size, sort, customUser);
            message = "숙소 목록 조회 성공";
        } else {
            result = accService.getAccSearch(page, size, sort, keyword, customUser);
            message = "숙소 검색 성공";
        }
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, message, result));
    }

    @GetMapping("/detail")
    public ResponseEntity<ResponseDto> getAccDetail(
            @RequestParam @Valid long id,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "숙소 상세 조회", accService.getAccDetail(id, customUser)));
    }

    @GetMapping("/distance")
    public ResponseEntity<ResponseDto> getAccSortByDistance(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam @Valid long id,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "가까운 거리 순 정렬 성공", accService.getAccSortByDistance(page, size, id, customUser)));
    }

    /** 지도용 숙소 목록 */
    @GetMapping("/simple")
    public ResponseEntity<ResponseDto> getAccSimpleList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "숙소 간단 목록 조회 성공", accService.getAccSimpleList(page, size))
        );
    }
}
