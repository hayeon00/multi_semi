package com.multi.travel.api.repository;

/*
 * Please explain the class!!!
 *
 * @filename    : TourSpotApiRepository
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 8. 토요일
 */

import com.multi.travel.acc.entity.Acc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccApiRepository extends JpaRepository<Acc, Long> {
}
