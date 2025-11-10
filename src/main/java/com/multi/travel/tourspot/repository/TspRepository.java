package com.multi.travel.tourspot.repository;

/*
 * Please explain the class!!!
 *
 * @filename    : TspRepository
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 9. 일요일
 */


import com.multi.travel.tourspot.entity.TourSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TspRepository extends JpaRepository<TourSpot,Long> {

    Optional<TourSpot> findByIdAndStatus(Long id, String status);

    Optional<TourSpot> findByContentId(Integer contentId);
}
