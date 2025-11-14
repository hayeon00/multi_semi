package com.multi.travel.acc.repository;

/*
 * Please explain the class!!!
 *
 * @filename    : AccRepository
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 9. 일요일
 */


import com.multi.travel.acc.dto.AccHasDistanceProjection;
import com.multi.travel.acc.entity.Acc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccRepository extends JpaRepository<Acc,Long> {
    Optional<Acc> findByIdAndStatus(long id, String status);

    Optional<Acc> findByContentId(Integer contentId);

    Page<Acc> findByStatus(String status, Pageable pageable);

    @Query(value = """
            SELECT a.id,
                   (6371 * acos(
                       cos(radians(:mapy)) * cos(radians(a.mapy)) *
                       cos(radians(a.mapx) - radians(:mapx)) +
                       sin(radians(:mapy)) * sin(radians(a.mapy))
                   )) AS distance
            FROM tb_acc a
            WHERE a.status = 'Y' AND a.id <> :id
            ORDER BY distance
            """, nativeQuery = true)
    List<Object[]> findNearestWithDistance(@Param("mapx") BigDecimal mapx,
                                           @Param("mapy") BigDecimal mapy,
                                           @Param("id") Long id,
                                           Pageable pageable);

    @Query(
            value = """
                    SELECT a.id,
               a.title,
               a.address,
               a.rec_count AS recCount,
               a.first_image AS firstImage,
               (
                   6371 * acos(
                       cos(radians(:mapy)) * cos(radians(a.mapy)) *
                       cos(radians(a.mapx) - radians(:mapx)) +
                       sin(radians(:mapy)) * sin(radians(a.mapy))
                   )
               ) AS distanceKm
        FROM tb_acc a
        WHERE a.status = 'Y'
        ORDER BY distanceKm
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM tb_acc a
        WHERE a.status = 'Y'
        """,
            nativeQuery = true
    )
    Page<AccHasDistanceProjection> findNearestWithDistanceAndStatus(
            @Param("mapx") BigDecimal mapx,
            @Param("mapy") BigDecimal mapy,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT a.id,
               a.title,
               a.address,
               a.rec_count AS recCount,
               a.first_image AS firstImage,
               (
                   6371 * acos(
                       cos(radians(:mapy)) * cos(radians(a.mapy)) *
                       cos(radians(a.mapx) - radians(:mapx)) +
                       sin(radians(:mapy)) * sin(radians(a.mapy))
                   )
               ) AS distanceKm
                    FROM tb_acc a
                    ORDER BY distanceKm
        """,
            countQuery = """

                    SELECT COUNT(*)
        FROM tb_acc a
        """,
            nativeQuery = true
    )
    Page<AccHasDistanceProjection> findNearestWithDistanceAdmin(
            @Param("mapx") BigDecimal mapx,
            @Param("mapy") BigDecimal mapy,
            Pageable pageable
    );

    @Query("""
    SELECT a FROM Acc a
    WHERE a.status = :status
    AND (
        LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(a.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
    )
    """)
    Page<Acc> statusAndSearch(@Param("status") String status,
                     @Param("keyword") String keyword,
                     Pageable pageable);

    @Query("""
    SELECT a FROM Acc a
    WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
         OR LOWER(a.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<Acc> search(@Param("keyword") String keyword,
                     Pageable pageable);

}
