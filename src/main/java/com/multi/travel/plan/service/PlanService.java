package com.multi.travel.plan.service;

import com.multi.travel.acc.repository.AccRepository;
import com.multi.travel.api.repository.TourSpotApiRepository;
import com.multi.travel.course.dto.CoursePlaceDto;
import com.multi.travel.course.entity.Course;
import com.multi.travel.course.entity.CourseItem;
import com.multi.travel.member.entity.Member;
import com.multi.travel.member.repository.MemberRepository;
import com.multi.travel.plan.dto.PlanDetailResDto;
import com.multi.travel.plan.dto.PlanReqDto;
import com.multi.travel.plan.entity.TripPlan;
import com.multi.travel.plan.repository.TripPlanRepository;
import com.multi.travel.tourspot.entity.TourSpot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
    private final AccRepository accRepository;

    public Long createTripPlan(PlanReqDto dto) {
        Member member = memberRepository.findByLoginId(dto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        TourSpot attraction = tourSpotApiRepository.findById(dto.getTourSpotId())
                .orElseThrow(() -> new IllegalArgumentException("관광지 정보를 찾을 수 없습니다."));

        TripPlan plan = TripPlan.builder()
                .title(dto.getTitle())
                .startLocation(attraction.getTitle())
                .startMapX(attraction.getMapx())
                .startMapY(attraction.getMapy())
                .numberOfPeople(dto.getNumberOfPeople())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .isAiPlan(false)
                .status('Y')
                .member(member)
                .build();

        TripPlan saved = tripPlanRepository.save(plan);
        return saved.getId();
    }


    @Transactional(readOnly = true)
    public PlanDetailResDto getTripPlanDetail(Long planId) {
        TripPlan plan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다."));

        List<CoursePlaceDto> coursePlaceDtos = new ArrayList<>();

        // 코스가 있는 경우
        if (plan.getCourse() != null) {
            Course course = plan.getCourse();

            List<CourseItem> items = course.getItems().stream()
                    .sorted(Comparator.comparingInt(CourseItem::getOrderNo))
                    .toList();

            for (CourseItem item : items) {
                CoursePlaceDto.CoursePlaceDtoBuilder builder = CoursePlaceDto.builder()
                        .id(item.getPlaceId())
                        .type(item.getPlaceType())
                        .orderNo(item.getOrderNo());

                if ("TOUR_SPOT".equals(item.getPlaceType())) {
                    tourSpotApiRepository.findById(item.getPlaceId())
                            .ifPresent(spot -> builder
                                    .title(spot.getTitle())
                                    .address(spot.getAddress())
                                    .mapx(spot.getMapx().toPlainString())
                                    .mapy(spot.getMapy().toPlainString())
                                    );
                } else if ("ACCOMMODATION".equals(item.getPlaceType())) {
                    accRepository.findById(item.getPlaceId())
                            .ifPresent(acc -> builder
                                    .title(acc.getTitle())
                                    .address(acc.getAddress())
                                    .mapx(acc.getMapx().toPlainString())
                                    .mapy(acc.getMapy().toPlainString())
                                    );
                }

                coursePlaceDtos.add(builder.build());
            }
        }

        return PlanDetailResDto.builder()
                .id(plan.getId())
                .title(plan.getTitle())
                .startLocation(plan.getStartLocation())
                .startMapX(plan.getStartMapX())
                .startMapY(plan.getStartMapY())
                .isAiPlan(plan.isAiPlan())
                .status(plan.getStatus())
                .numberOfPeople(plan.getNumberOfPeople())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .memberName(plan.getMember().getUsername())
                .coursePlaces(coursePlaceDtos)
                .build();
    }




}