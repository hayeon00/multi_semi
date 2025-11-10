package com.multi.travel.acc.repository;

/*
 * Please explain the class!!!
 *
 * @filename    : AccRepository
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 9. 일요일
 */


import com.multi.travel.acc.entity.Acc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccRepository extends JpaRepository<Acc, Long> {
    Optional<Acc> findByIdAndStatus(long id, String status);

    Optional<Acc> findByContentId(Integer contentId);

    Page<Acc> findByStatus(String status, Pageable pageable);

    @Query(value = """
                    /* language=SQL */
            SELECT a.id, 
                   (6371 * acos(
                       cos(radians(:mapy)) * cos(radians(a.mapy)) *
                       cos(radians(a.mapx) - radians(:mapx)) +
                       sin(radians(:mapy)) * sin(radians(a.mapy))
                   )) AS distance
            FROM tb_acc a
            WHERE a.status = 'Y' AND a.id <> :id
            ORDER BY distance ASC
            """, nativeQuery = true)
    List<Object[]> findNearestWithDistance(@Param("mapx") BigDecimal mapx,
                                           @Param("mapy") BigDecimal mapy,
                                           @Param("id") Long id,
                                           Pageable pageable);
}
