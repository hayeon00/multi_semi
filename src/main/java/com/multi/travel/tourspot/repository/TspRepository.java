package com.multi.travel.tourspot.repository;

/*
 * Please explain the class!!!
 *
 * @filename    : TspRepository
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 9. 일요일
 */


import com.multi.travel.tourspot.dto.TspHasDistanceProjection;
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

    Page<TourSpot> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    @Query(value = """
                SELECT t.id,
                       (6371 * acos(
                           cos(radians(:mapy)) * cos(radians(t.mapy)) *
                           cos(radians(t.mapx) - radians(:mapx)) +
                           sin(radians(:mapy)) * sin(radians(t.mapy))
                       )) AS distance
                FROM tb_tsp t
                WHERE t.status = 'Y' AND t.id <> :id
                ORDER BY distance
            """, nativeQuery = true)
    List<Object[]> findNearestWithDistance(@Param("mapx") BigDecimal mapx,
                                           @Param("mapy") BigDecimal mapy,
                                           @Param("id") Long id,
                                           Pageable pageable);



    @Query(value = """
                SELECT t.id, t.title, t.address, t.rec_count as recCount, t.first_image as firstImage,
                       (6371 * acos(
                           cos(radians(:mapy)) * cos(radians(t.mapy)) *
                           cos(radians(t.mapx) - radians(:mapx)) +
                           sin(radians(:mapy)) * sin(radians(t.mapy))
                       )) AS distanceKm
                FROM tb_tsp t
                WHERE t.status = 'Y' AND t.id <> :id
                ORDER BY distanceKm
            """, nativeQuery = true)
    List<TspHasDistanceProjection> findNearestWithDistanceRefactor(@Param("mapx") BigDecimal mapx,
                                                                   @Param("mapy") BigDecimal mapy,
                                                                   @Param("id") Long id,
                                                                   Pageable pageable);


    /** 출발 좌표로 관광지 찾기 */
    Optional<TourSpot> findByMapxAndMapy(BigDecimal mapx, BigDecimal mapy);

}
