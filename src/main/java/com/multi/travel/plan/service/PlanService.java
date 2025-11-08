package com.multi.travel.plan.service;

import com.multi.travel.attraction.entity.Attraction;
import com.multi.travel.attraction.repository.AttractionRepository;
import com.multi.travel.member.entity.Member;
import com.multi.travel.member.repository.MemberRepository;
import com.multi.travel.plan.dto.PlanReqDto;
import com.multi.travel.plan.entity.TripPlan;
import com.multi.travel.plan.repository.TripPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : PlanService
 * @since : 2025. 11. 8. 토요일
 */

@Service
@RequiredArgsConstructor
public class PlanService {

    private final TripPlanRepository tripPlanRepository;
    private final AttractionRepository attractionRepository;
    private final MemberRepository memberRepository;

    public Long createTripPlan(PlanReqDto dto) {

        //사용자 조회
        Member member = memberRepository.findByMemberId(dto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        //관광지 조회 (출발 위치용)
        Attraction attraction = attractionRepository.findById(dto.getAttractionId())
                .orElseThrow(() -> new IllegalArgumentException("관광지 정보가 없습니다."));

        TripPlan plan = TripPlan.builder()
                .title(dto.getTitle())
                .startLocation(attraction.getLocation())
                .numberOfPeople(dto.getNumberOfPeople())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .member(member)
                .build();

        TripPlan saved = tripPlanRepository.save(plan);
        return saved.getId();
    }

}
