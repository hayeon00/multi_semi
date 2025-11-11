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
import com.multi.travel.category.CategoryRepository;
import com.multi.travel.category.entity.Category;
import com.multi.travel.common.exception.AccommodationNotFound;
import com.multi.travel.common.exception.CategoryNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccService {
    private final AccRepository accRepository;
    private final CategoryRepository categoryRepository;

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


        Pageable pageable = PageRequest.of(page, size);


        List<Object[]> results = accRepository.findNearestWithDistance(criteria.getMapx(), criteria.getMapy(), id, pageable);
        return results.stream()
                .map(obj -> {
                    Long accId = (Long) obj[0];
                    Double distance = (Double) obj[1];
                    Acc acc = accRepository.findById(accId).orElseThrow(() -> new AccommodationNotFound(accId));
                    acc.setDistanceKm(distance);
                    return AccEntityToDTO(acc, distance);
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
                .catCode("acc")
                .createdAt(acc.getCreatedAt())
                .modifiedAt(acc.getModifiedAt())
                .build();
    }

    public AccDTO registAcc(AccDTO accDTO) {
        Category category = categoryRepository.findById(accDTO.getCatCode()).orElseThrow(() -> new CategoryNotFoundException(accDTO.getCatCode()));
        Acc newAcc = Acc.builder()
                .address(accDTO.getAddress())
                .title(accDTO.getTitle())
                .tel(accDTO.getTel())
                .mapx(accDTO.getMapx())
                .mapy(accDTO.getMapy())
                .areacode(accDTO.getAreacode())
                .sigungucode(accDTO.getSigungucode())
                .lDongRegnCd(accDTO.getLDongRegnCd())
                .contentId(accDTO.getContentId())
                .category(category)
                .status("Y")
                .recCount(0)
                .build();
        accRepository.save(newAcc);
        return AccEntityToDTO(newAcc, 0.0);
    }

    @Transactional
    public AccDTO updateAcc(AccDTO accDTO) {
        Acc acc = accRepository.findById(accDTO.getId()).orElseThrow(() -> new AccommodationNotFound(accDTO.getId()));
        acc.updateInfo(accDTO);
        return AccEntityToDTO(acc, 0.0);
    }

    @Transactional
    public Object deleteAcc(@Valid Long accId) {
        Acc acc = accRepository.findById(accId).orElseThrow(() -> new AccommodationNotFound(accId));
        if(acc.getStatus().equals("Y")) {
            acc.changeStatus();
        }
        return AccEntityToDTO(acc, 0.0);
    }
}
