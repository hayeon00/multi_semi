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
import com.multi.travel.tourspot.dto.ResDistanceTspDTO;
import com.multi.travel.tourspot.dto.ResTspDTO;
import com.multi.travel.tourspot.dto.TourSpotDTO;
import com.multi.travel.tourspot.dto.TspHasDistanceProjection;
import com.multi.travel.tourspot.entity.TourSpot;
import com.multi.travel.tourspot.repository.TspRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TspService {

    private final TspRepository tspRepository;
    private final ApiService apiService;


    public List<ResTspDTO> getTourSpotList(int page, int size, String sort, CustomUser customUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        if (RoleUtils.hasRole(customUser, RoleUtils.ADMIN)) {
            return convertToResTspDTO(tspRepository.findAll(pageable).getContent());
        }
        return convertToResTspDTO(tspRepository.findByStatus("Y", pageable).getContent());
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
        apiService.insertDetail(tsp.getContentId(), tsp.getCategory().getCatCode());
        TourSpot updatedTsp = tspRepository.findById(id)
                .orElseThrow(() -> new TourSpotNotFoundException(id));
        return TourSpotEntityToDTO(updatedTsp);
    }


    public List<ResTspDTO> getTspSearch(int page, int size, String sort, String keyword, CustomUser customUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        Page<TourSpot> tspPage;
        if (RoleUtils.hasRole(customUser, RoleUtils.ADMIN)) {
            tspPage = tspRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else {
            tspPage = tspRepository.findByStatusAndTitleContainingIgnoreCase("Y", keyword, pageable);
        }
        return convertToResTspDTO(tspPage.getContent());
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
                .cat_code("tsp")
                .createdAt(spot.getCreatedAt())
                .modifiedAt(spot.getModifiedAt())
                .build();
    }
}
