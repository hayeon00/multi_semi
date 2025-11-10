package com.multi.travel.acc.serivce;

/*
 * Please explain the class!!!
 *
 * @filename    : AccService
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 9. 일요일
 */

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

@Service
@RequiredArgsConstructor
public class AccService {
    private final AccRepository accRepository;

    public Page<Acc> getAccListPaging(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        return accRepository.findAll(pageable);
    }

    public Acc getAccDetail(@Valid long id) {
        return accRepository.findByIdAndStatus(id, "Y")
                .orElseThrow(()->new AccommodationNotFound(id));
    }
}
