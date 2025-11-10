package com.multi.travel.acc.repository;

/*
 * Please explain the class!!!
 *
 * @filename    : AccRepository
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 9. 일요일
 */


import com.multi.travel.acc.entity.Acc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccRepository extends JpaRepository<Acc,Long> {
    Optional<Acc> findByIdAndStatus(long id, String status);

}
