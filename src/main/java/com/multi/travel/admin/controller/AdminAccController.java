package com.multi.travel.admin.controller;

/*
 * Please explain the class!!!
 *
 * @filename    : AdminAccController
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 11. 화요일
 */


import com.multi.travel.acc.dto.AccDTO;
import com.multi.travel.acc.serivce.AccService;
import com.multi.travel.common.ResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/accommodations")
@RequiredArgsConstructor
public class AdminAccController {
    private final AccService accService;

    @PostMapping("/regist")
    public ResponseEntity<ResponseDto> registAcc(@ModelAttribute AccDTO accDTO) {
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "숙소 추가 성공", accService.registAcc(accDTO)));
    }

    @PutMapping("/update")
    public ResponseEntity<ResponseDto> updateAcc(@ModelAttribute AccDTO accDTO) {
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "숙소 수정 성공", accService.updateAcc(accDTO)));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDto> deleteAcc(@RequestParam @Valid Long accId) {
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "숙소 삭제 성공", accService.deleteAcc(accId)));

    }
}
