package com.multi.travel.admin.repository;

import com.multi.travel.tourspot.entity.TourSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : chang
 * @filename : TourSpotRepository
 * @since : 2025-11-11 화요일
 */
public interface TourSpotRepository extends JpaRepository<TourSpot,Long> {

    List<TourSpot> findByTitleContainingIgnoreCase(String title);
}
