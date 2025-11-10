package com.multi.travel.tourspot.service;

/*
 * Please explain the class!!!
 *
 * @filename    : TspService
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 9. 일요일
 */


import com.multi.travel.category.CategoryRepository;
import com.multi.travel.common.exception.TourSpotNotFoundException;
import com.multi.travel.tourspot.dto.TourSpotDTO;
import com.multi.travel.tourspot.entity.TourSpot;
import com.multi.travel.tourspot.repository.TspRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TspService {

    private final TspRepository tspRepository;
    private final CategoryRepository categoryRepository;


    public List<TourSpotDTO> getTourSpotList(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        return TourSpotListEntityToDto(tspRepository.findByStatus("Y", pageable));
    }


    public TourSpotDTO getTourSpotDetail(Long id) {
        return TourSpotEntityToDTO(tspRepository.findByIdAndStatus(id, "Y")
                .orElseThrow(() -> new TourSpotNotFoundException(id)), 0.0);

    }

    public List<TourSpotDTO> getTspSearch(int page, int size, String sort, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        Page<TourSpot> tsps = tspRepository.findByStatusAndTitleContainingIgnoreCase("Y", keyword, pageable);

        return TourSpotListEntityToDto(tsps);
    }

    public List<TourSpotDTO> getTspSortByDistance(int page, int size, String sort, Long id) {
        TourSpot criteria = tspRepository.findByIdAndStatus(id, "Y")
                .orElseThrow(() -> new TourSpotNotFoundException(id));

        BigDecimal mapx = criteria.getMapx();
        BigDecimal mapy = criteria.getMapy();

        Pageable pageable = PageRequest.of(page, size);

        List<Object[]> results = tspRepository.findNearestWithDistance(mapx, mapy, id, pageable);

        List<TourSpotDTO> dtos = results.stream()
                .map(obj -> {
                    TourSpot spot = (TourSpot) obj[0];
                    Double distance = (Double) obj[1];
                    spot.setDistanceKm(distance);
                    return TourSpotEntityToDTO(spot, distance);
                })
                .collect(Collectors.toList());

        return dtos;
    }

    private static List<TourSpotDTO> TourSpotListEntityToDto(Page<TourSpot> tsps) {
        return tsps.stream()
                .map(tsp->TourSpotEntityToDTO(tsp, 0.0))
                .collect(Collectors.toList());
    }

    private static TourSpotDTO TourSpotEntityToDTO(TourSpot spot, Double distance) {
        return TourSpotDTO.builder()
                .id(spot.getId())
                .title(spot.getTitle())
                .address(spot.getAddress())
                .mapx(spot.getMapx())
                .mapy(spot.getMapy())
                .tel(spot.getTel())
                .firstImage(spot.getFirstImage())
                .firstImage2(spot.getFirstImage2())
                .areacode(spot.getAreacode())
                .recCount(Optional.ofNullable(spot.getRecCount()).orElse(0))
                .sigungucode(spot.getSigungucode())
                .lDongRegnCd(spot.getLDongRegnCd())
                .contentId(spot.getContentId())
                .status(spot.getStatus())
                .distanceMeter(distance * 1000)
                .cat_code("tsp")
                .createdAt(spot.getCreatedAt())
                .modifiedAt(spot.getModifiedAt())
                .build();
    }

}
