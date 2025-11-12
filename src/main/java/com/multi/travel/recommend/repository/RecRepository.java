package com.multi.travel.recommend.repository;

/*
 * Please explain the class!!!
 *
 * @filename    : RecRepository
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 11. 화요일
 */


import com.multi.travel.recommend.entity.Recommend;
import jakarta.validation.Valid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecRepository extends JpaRepository<Recommend, Long> {

    Optional<Recommend> findByMember_IdAndTargetIdAndCategory_CatCode(@Valid Long userId, @Valid Long targetId, @Valid String catCode);
}
