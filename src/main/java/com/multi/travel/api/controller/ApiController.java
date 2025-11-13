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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {

    private final ApiService apiService;

    @GetMapping("/tourlist")
    public ResponseEntity<Object> collectAllTourSpots(@RequestParam int type) {

        apiService.collectAllTourSpots(type);

        return ResponseEntity.ok("All tour spots collected and inserted.");
    }

    @GetMapping("/acc")
    public ResponseEntity<Object> collectAllAccs() {

        apiService.collectAllAccs();

        return ResponseEntity.ok("All Acc collected and inserted.");
    }

    @GetMapping("/detail")
    public ResponseEntity<Object> collectAllDetails(@RequestParam int contentId, @RequestParam String type) {

        apiService.insertDetail(contentId, "type");
        return ResponseEntity.ok("All details collected and inserted.");
    }
}

