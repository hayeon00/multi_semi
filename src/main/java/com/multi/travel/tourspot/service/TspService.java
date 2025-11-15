package com.multi.travel.tourspot.service;

/*
 * Please explain the class!!!
 *
 * @filename    : TspService
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 9. 일요일
 */


import com.multi.travel.api.service.ApiService;
import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.common.exception.TourSpotNotFoundException;
import com.multi.travel.common.util.RoleUtils;
import com.multi.travel.tourspot.dto.*;
import com.multi.travel.tourspot.entity.TourSpot;
import com.multi.travel.tourspot.repository.TspRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TspService {

    private final TspRepository tspRepository;
    private final ApiService apiService;


    public Map<String, Object> getTourSpotList(int page, int size, String sort, CustomUser customUser) {
        Page<TourSpot> tspPage;
        Pageable pageable;
        if (sort.equals("recCount")) {
            pageable = PageRequest.of(page, size, Sort.by(sort).descending());
        } else {
            pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        }

        if (RoleUtils.hasRole(customUser, RoleUtils.ADMIN)) {
            tspPage = tspRepository.findAll(pageable);
        } else {
            tspPage = tspRepository.findByStatus("Y", pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalPages", tspPage.getTotalPages());
        response.put("contents", convertToResTspDTO(tspPage.getContent()));
        return response;
    }


    public Map<String, Object> getTspSearch(int page, int size, String sort, String keyword, CustomUser customUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        Page<TourSpot> tspPage;
        if (RoleUtils.hasRole(customUser, RoleUtils.ADMIN)) {
            tspPage = tspRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else {
            tspPage = tspRepository.findByStatusAndTitleContainingIgnoreCase("Y", keyword, pageable);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("totalPages", tspPage.getTotalPages());
        response.put("contents", convertToResTspDTO(tspPage.getContent()));
        return response;
    }

    public TourSpotDTO getTourSpotDetail(Long id, CustomUser customUser) {
        TourSpot tsp;
        if (RoleUtils.hasRole(customUser, RoleUtils.ADMIN)) {
            tsp = tspRepository.findById(id)
                    .orElseThrow(() -> new TourSpotNotFoundException(id));
        } else {
            tsp = tspRepository.findByIdAndStatus(id, "Y")
                    .orElseThrow(() -> new TourSpotNotFoundException(id));
        }
        if(tsp.getDescription() == null || tsp.getHomepage() == null) {
            apiService.insertDetail(tsp.getContentId(), tsp.getCategory().getCatCode());
        }

        TourSpot updatedTsp = tspRepository.findById(id)
                .orElseThrow(() -> new TourSpotNotFoundException(id));
        return TourSpotEntityToDTO(updatedTsp);
    }



    public List<ResDistanceTspDTO> getTspSortByDistance(int page, int size, Long id) { //리팩토링
        TourSpot criteria = tspRepository.findByIdAndStatus(id, "Y")
                .orElseThrow(() -> new TourSpotNotFoundException(id));

        Pageable pageable = PageRequest.of(page, size);
        List<TspHasDistanceProjection> lists = tspRepository.findNearestWithDistanceRefactor(criteria.getMapx(), criteria.getMapy(), id, pageable);

        return convertToResDistanceTspDTO(lists);
    }


    private static List<ResDistanceTspDTO> convertToResDistanceTspDTO(List<TspHasDistanceProjection> lists) {
        return lists.stream()
                .map(list -> ResDistanceTspDTO.builder()
                        .id(list.getId())
                        .title(list.getTitle())
                        .address(list.getAddress())
                        .recCount(list.getRecCount())
                        .firstImage(list.getFirstImage())
                        .distanceMeter(list.getDistanceKm()*1000)
                        .build()
                ).toList();
    }

    private static List<ResTspDTO> convertToResTspDTO(List<TourSpot> lists) {
        return lists.stream()
                .map(list -> ResTspDTO.builder()
                        .id(list.getId())
                        .title(list.getTitle())
                        .address(list.getAddress())
                        .recCount(list.getRecCount())
                        .firstImage(list.getFirstImage())
                        .mapx(list.getMapx())
                        .mapy(list.getMapy())
                        .build()
                ).toList();
    }

    private static TourSpotDTO TourSpotEntityToDTO(TourSpot spot) {
        return TourSpotDTO.builder()
                .id(spot.getId())
                .address(spot.getAddress())
                .title(spot.getTitle())
                .description(spot.getDescription())
                .homepage(spot.getHomepage())
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
                .catCode("tsp")
                .createdAt(spot.getCreatedAt())
                .modifiedAt(spot.getModifiedAt())
                .build();
    }


    public Map<String, Object> getTspSimpleList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TourSpot> tspPage = tspRepository.findByStatus("Y", pageable);

        List<ResTspSimpleDTO> list = tspPage.getContent().stream()
                .map(t -> ResTspSimpleDTO.builder()
                        .id(t.getId())
                        .title(t.getTitle())
                        .mapx(t.getMapx())
                        .mapy(t.getMapy())
                        .build())
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", tspPage.getTotalPages());
        result.put("contents", list);
        return result;
    }
}
