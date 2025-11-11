package com.multi.travel.admin.service;

import com.multi.travel.admin.controller.dto.TourSpotReqDto;
import com.multi.travel.admin.repository.TourSpotRepository;
import com.multi.travel.tourspot.entity.TourSpot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Please explain the class!!!
 *
 * @author : chang
 * @filename : adminService
 * @since : 2025-11-10 월요일
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final TourSpotRepository tourSpotRepository;

    @Transactional
    public void insertTourSpot(TourSpotReqDto dto) {

        TourSpot tourSpot = TourSpot.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .address(dto.getAddress())
                .tel(dto.getTel())
                .mapx(dto.getMapx())
                .mapy(dto.getMapy())
                .areacode(dto.getAreacode())
                .sigungucode(dto.getSigungucode())
                .lDongRegnCd(dto.getLDongRegnCd())
                .status("Y")
                .recCount(0)
                .build();


        tourSpotRepository.save(tourSpot);
    }

    public void deleteSpot(Long id) {
        TourSpot tourSpot = tourSpotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관광지입니다."));

        tourSpot.setStatus("N");
        tourSpotRepository.save(tourSpot);

    }
}
