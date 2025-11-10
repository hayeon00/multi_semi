package com.multi.travel.acc.serivce;

/*
 * Please explain the class!!!
 *
 * @filename    : AccService
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 9. 일요일
 */

import com.multi.travel.acc.dto.AccDTO;
import com.multi.travel.acc.entity.Acc;
import com.multi.travel.acc.repository.AccRepository;
import com.multi.travel.common.exception.AccommodationNotFound;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccService {
    private final AccRepository accRepository;

    public List<AccDTO> getAccListPaging(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        return TourSpotListEntityToDto(accRepository.findByStatus("Y", pageable));
    }

    public AccDTO getAccDetail(@Valid long id) {
        Acc entity = accRepository.findByIdAndStatus(id, "Y")
                .orElseThrow(() -> new AccommodationNotFound(id));
        return AccEntityToDTO(entity, 0.0);

    }

    private static List<AccDTO> TourSpotListEntityToDto(Page<Acc> accs) {
        return accs.stream()
                .map(acc -> AccEntityToDTO(acc, 0.0))
                .collect(Collectors.toList());
    }

    public List<AccDTO> getAccSortByDistance(int page, int size, @Valid long id) {
        Acc criteria = accRepository.findByIdAndStatus(id, "Y")
                .orElseThrow(() -> new AccommodationNotFound(id));

        BigDecimal mapx = criteria.getMapx();
        BigDecimal mapy = criteria.getMapy();

        Pageable pageable = PageRequest.of(page, size);


        List<Object[]> results = accRepository.findNearestWithDistance(mapx, mapy, id, pageable);
        return results.stream()
                .map(obj -> {
                    Acc spot = (Acc) obj[0];
                    Double distance = (Double) obj[1];
                    spot.setDistanceKm(distance);
                    return AccEntityToDTO(spot, distance);
                })
                .collect(Collectors.toList());

    }

    private static AccDTO AccEntityToDTO(Acc acc, Double distance) {
        return AccDTO.builder()
                .id(acc.getId())
                .title(acc.getTitle())
                .address(acc.getAddress())
                .mapx(acc.getMapx())
                .mapy(acc.getMapy())
                .tel(acc.getTel())
                .firstImage(acc.getFirstImage())
                .firstImage2(acc.getFirstImage2())
                .areacode(acc.getAreacode())
                .recCount(acc.getRecCount() != null ? acc.getRecCount() : 0)
                .sigungucode(acc.getSigungucode())
                .lDongRegnCd(acc.getLDongRegnCd())
                .contentId(acc.getContentId())
                .status(acc.getStatus())
                .distanceMeter(distance * 1000)
                .cat_code("acc")
                .createdAt(acc.getCreatedAt())
                .modifiedAt(acc.getModifiedAt())
                .build();
    }

}
