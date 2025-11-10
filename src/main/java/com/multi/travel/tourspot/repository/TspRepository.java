package com.multi.travel.tourspot.repository;

/*
 * Please explain the class!!!
 *
 * @filename    : TspRepository
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 9. 일요일
 */


import com.multi.travel.tourspot.entity.TourSpot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TspRepository extends JpaRepository<TourSpot,Long> {

    Optional<TourSpot> findByIdAndStatus(Long id, String status);

    Page<TourSpot> findByStatus(String status, Pageable pageable);
    Optional<TourSpot> findByContentId(Integer contentId);

    Page<TourSpot> findByStatusAndTitleContainingIgnoreCase(String status, String keyword, Pageable pageable);

    @Query("""
                SELECT t, 
                       (6371 * acos(
                           cos(radians(:mapy)) * cos(radians(t.mapy)) *
                           cos(radians(t.mapx) - radians(:mapx)) +
                           sin(radians(:mapy)) * sin(radians(t.mapy))
                       )) AS distance
                FROM TourSpot t
                WHERE t.status = 'Y' AND t.id <> :id
                ORDER BY distance ASC
            """)
    List<Object[]> findNearestWithDistance(@Param("mapx") BigDecimal mapx,
                                           @Param("mapy") BigDecimal mapy,
                                           @Param("id") Long id,
                                           Pageable pageable);

}
