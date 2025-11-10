package com.multi.travel.tourspot.service;

/*
 * Please explain the class!!!
 *
 * @filename    : TspService
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 9. 일요일
 */


import com.multi.travel.common.exception.TourSpotNotFoundException;
import com.multi.travel.tourspot.entity.TourSpot;
import com.multi.travel.tourspot.repository.TspRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TspService {

    private final TspRepository tspRepository;


    public Page<TourSpot> getTourSpotList(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        return tspRepository.findAll(pageable);
    }


    public TourSpot getTourSpotDetail(Long id) {
        return tspRepository.findByIdAndStatus(id, "Y")
                .orElseThrow(() -> new TourSpotNotFoundException(id));
    }
}
