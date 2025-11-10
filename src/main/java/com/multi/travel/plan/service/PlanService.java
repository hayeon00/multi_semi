package com.multi.travel.plan.service;

import com.multi.travel.api.repository.TourSpotApiRepository;
import com.multi.travel.member.entity.Member;
import com.multi.travel.member.repository.MemberRepository;
import com.multi.travel.plan.dto.PlanReqDto;
import com.multi.travel.plan.entity.TripPlan;
import com.multi.travel.plan.repository.TripPlanRepository;
import com.multi.travel.tourspot.entity.TourSpot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 여행 계획 생성 서비스
 * 관광지 상세 페이지에서 여행 계획 생성 시,
 * 해당 관광지를 출발 위치로 자동 설정
 *
 * @author : hayeon
 * @since : 2025. 11. 09
 */

@Service
@RequiredArgsConstructor
@Transactional
public class PlanService {

    private final TripPlanRepository tripPlanRepository;
    private final MemberRepository memberRepository;
    private final TourSpotApiRepository tourSpotApiRepository;

    public Long createTripPlan(PlanReqDto dto) {

        Member member = memberRepository.findByLoginId(dto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        TourSpot attraction = tourSpotApiRepository.findById(dto.getTourSpotId())
                .orElseThrow(() -> new IllegalArgumentException("관광지 정보를 찾을 수 없습니다."));

        TripPlan plan = TripPlan.builder()
                .title(dto.getTitle())
                .startLocation(attraction.getTitle()) // 관광지명
                .startMapX(attraction.getMapx())       // 경도
                .startMapY(attraction.getMapy())       // 위도
                .numberOfPeople(dto.getNumberOfPeople())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .isAiPlan(false)                       // 기본값: 수동 계획
                .status('Y')                           // 활성 상태
                .member(member)
                .build();

        TripPlan saved = tripPlanRepository.save(plan);

        return saved.getId();
    }
}
