package com.multi.travel.plan.repository;

import com.multi.travel.plan.entity.TripPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : TripPlanRepository
 * @since : 2025. 11. 8. 토요일
 */
public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {


    /** ✅ 로그인한 회원이 작성한 여행계획 전체조회 (최신순 - id 기준) */
    List<TripPlan> findAllByMember_LoginId(String loginId);

    TripPlan findByCourse_CourseId(Long targetId);
}
