package com.multi.travel.api.controller;

/*
 * Please explain the class!!!
 *
 * @filename    : TourSpotController
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 8. 토요일
 */

import com.multi.travel.api.service.ApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {

    private final ApiService apiService;

    @GetMapping("/tourlist")
    public ResponseEntity<Object> collectAllTourSpots() {

        apiService.collectAllTourSpots();

        return ResponseEntity.ok("All tour spots collected and inserted.");
    }

    @GetMapping("/acc")
    public ResponseEntity<Object> collectAllAccs() {

        apiService.collectAllAccs();

        return ResponseEntity.ok("All Acc collected and inserted.");
    }

}

