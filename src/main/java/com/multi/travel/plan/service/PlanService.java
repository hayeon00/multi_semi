package com.multi.travel.plan.service;

import com.multi.travel.acc.repository.AccRepository;
import com.multi.travel.api.repository.TourSpotApiRepository;
import com.multi.travel.category.CategoryRepository;
import com.multi.travel.category.entity.Category;
import com.multi.travel.course.dto.CourseItemReqDto;
import com.multi.travel.course.dto.CoursePlaceDto;
import com.multi.travel.course.entity.Course;
import com.multi.travel.course.entity.CourseItem;
import com.multi.travel.course.repository.CourseRepository;
import com.multi.travel.member.entity.Member;
import com.multi.travel.member.repository.MemberRepository;
import com.multi.travel.plan.dto.PlanDetailResDto;
import com.multi.travel.plan.dto.PlanReqDto;
import com.multi.travel.plan.entity.TripPlan;
import com.multi.travel.plan.repository.TripPlanRepository;
import com.multi.travel.tourspot.entity.TourSpot;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanService {

    private final TripPlanRepository tripPlanRepository;
    private final MemberRepository memberRepository;
    private final TourSpotApiRepository tourSpotApiRepository;
    private final CourseRepository courseRepository;
    private final AccRepository accRepository;
    private final CategoryRepository categoryRepository;


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

        if (plan.getCourse() != null) {
            Course course = plan.getCourse();
            List<CourseItem> items = course.getItems().stream()
                    .sorted(Comparator.comparingInt(CourseItem::getOrderNo))
                    .toList();

            for (CourseItem item : items) {
                String categoryCode = item.getCategory().getCatCode();

                CoursePlaceDto.CoursePlaceDtoBuilder builder = CoursePlaceDto.builder()
                        .id(item.getPlaceId())
                        .type(categoryCode)
                        .orderNo(item.getOrderNo())
                        .dayNo(item.getDayNo());

                if ("tsp".equals(categoryCode)) {
                    tourSpotApiRepository.findById(item.getPlaceId())
                            .ifPresent(spot -> builder
                                    .title(spot.getTitle())
                                    .address(spot.getAddress())
                                    .mapx(spot.getMapx().toPlainString())
                                    .mapy(spot.getMapy().toPlainString())
                            );
                } else if ("acc".equals(categoryCode)) {
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

    public void updateTripPlan(Long planId, PlanReqDto dto, String requesterId) {
        TripPlan plan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다."));

        if (!plan.getMember().getLoginId().equals(requesterId)) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        // 출발지 관광지 정보 가져오기
        TourSpot startSpot = tourSpotApiRepository.findById(dto.getTourSpotId())
                .orElseThrow(() -> new IllegalArgumentException("관광지 정보를 찾을 수 없습니다."));

        // 여행 기본 정보 업데이트
        plan.update(
                dto.getTitle(),
                dto.getNumberOfPeople(),
                dto.getStartDate(),
                dto.getEndDate(),
                startSpot.getTitle(),
                startSpot.getMapx(),
                startSpot.getMapy()
        );

        // 🔄 새로운 Course 생성
        Course newCourse = Course.builder()
                .creator(plan.getMember())
                .status("Y")
                .build();

        // 📦 CourseItem 추가
        for (CourseItemReqDto itemDto : dto.getCourse().getItems()) {
            CourseItem item = new CourseItem();
            item.setCourse(newCourse);  // 연관관계 설정
            item.setPlaceId(itemDto.getPlaceId());
            item.setOrderNo(itemDto.getOrderNo());
            item.setDayNo(itemDto.getDayNo());

            // 카테고리 설정
            Category category = categoryRepository.findById(itemDto.getCategoryCode())
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다. code=" + itemDto.getCategoryCode()));
            item.setCategory(category);

            newCourse.addItem(item);  // 양방향 연관관계 편의 메서드
        }

        // ✅ 코스를 먼저 저장해야 Hibernate 에러 방지됨
        Course savedCourse = courseRepository.save(newCourse);

        // 🔗 저장된 코스를 여행 계획에 연결
        plan.setCourse(savedCourse);
    }


    public void deleteTripPlan(Long planId, String requesterId) {
        TripPlan plan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다."));

        if (!plan.getMember().getLoginId().equals(requesterId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        tripPlanRepository.delete(plan);
    }

    @Transactional
    public void attachCourse(Long planId, Long courseId) {
        TripPlan plan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("계획을 찾을 수 없습니다. id=" + planId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("코스를 찾을 수 없습니다. id=" + courseId));

        plan.setCourse(course);

        // 날짜(startDate, endDate)는 그대로 유지됨 (변경 X)
        tripPlanRepository.save(plan);
    }

    /**
     * 사용자 ID로 여행 계획 목록 조회 (마이페이지 용도)
     */
    @Transactional(readOnly = true)
    public List<PlanDetailResDto> getPlansByUser(String uesrLoginId) {
        List<TripPlan> plans = tripPlanRepository.findAllByMember_LoginId(uesrLoginId);
        return plans.stream()
                .map(this::toPlanDetailDto)
                .collect(Collectors.toList());
    }

    /**
     * TripPlan → PlanDetailResDto 변환
     */
    private PlanDetailResDto toPlanDetailDto(TripPlan plan) {
        List<CoursePlaceDto> coursePlaceDtos = new ArrayList<>();

        if (plan.getCourse() != null) {
            coursePlaceDtos = plan.getCourse().getItems().stream()
                    .map(item -> CoursePlaceDto.builder()
                            .id(item.getPlaceId())
                            .type(item.getCategory().getCatCode())
                            .orderNo(item.getOrderNo())
                            .dayNo(item.getDayNo())
                            .title("장소명") // 필요 시 조회하여 추가
                            .address("주소")
                            .mapx("0.0")
                            .mapy("0.0")
                            .build())
                    .collect(Collectors.toList());
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