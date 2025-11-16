package com.multi.travel.course.service;

import com.multi.travel.acc.entity.Acc;
import com.multi.travel.acc.repository.AccRepository;
import com.multi.travel.category.CategoryRepository;
import com.multi.travel.category.entity.Category;
import com.multi.travel.course.dto.*;
import com.multi.travel.course.entity.Course;
import com.multi.travel.course.entity.CourseItem;
import com.multi.travel.course.repository.CourseItemRepository;
import com.multi.travel.course.repository.CourseRepository;
import com.multi.travel.member.entity.Member;
import com.multi.travel.plan.entity.TripPlan;
import com.multi.travel.plan.repository.TripPlanRepository;
import com.multi.travel.tourspot.entity.TourSpot;
import com.multi.travel.tourspot.repository.TspRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : CourseService
 * @since : 2025-11-08 토요일
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseItemRepository itemRepository;
    private final TripPlanRepository tripPlanRepository;
    private final CategoryRepository categoryRepository;
    private final TspRepository tspRepository;
    private final AccRepository accRepository;

    /** 코스 생성 */
    public CourseResDto createCourse(CourseReqDto dto) {

        TripPlan plan = tripPlanRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new EntityNotFoundException("해당 ID(" + dto.getPlanId() + ")의 계획을 찾을 수 없습니다."));


        // plan의 member 정보를 가져와 course에 주입
        Member creator = plan.getMember();

        Course course = Course.builder()
                .status("Y")
                .creator(creator)   // 작성자 설정
                .build();
        plan.setCourse(course);
        tripPlanRepository.save(plan);

        /* tripPlanRepository.save(plan) -------------------------------------------------------------------------------
            JPA에서 @ManyToOne 관계는 기본적으로 단방향 저장이다.
            즉, 자식(TripPlan)이 부모(Course)를 참조하더라도 부모를 먼저 save하지 않으면 외래키가 null로 남는다.
            cascade가 설정되어 있으면 자동 저장되지만, 그렇지 않은 경우는 트랜잭션 내에서 직접 save() 한 번 호출로 해결하는 것.
        ------------------------------------------------------------------------------------------------------------- */


        // 아이템 추가
        dto.getItems().forEach(itemDto -> {
            if (itemDto.getCategoryCode() == null || itemDto.getCategoryCode().isBlank()) {
                throw new IllegalArgumentException("카테고리 코드가 누락되었습니다. placeId=" + itemDto.getPlaceId());
            }

            Category category = categoryRepository.findById(itemDto.getCategoryCode())
                    .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다. code=" + itemDto.getCategoryCode()));

            CourseItem item = CourseItem.builder()
                    .course(course)
                    .category(category)
                    .placeId(itemDto.getPlaceId())
                    .orderNo(itemDto.getOrderNo())
                    .dayNo(itemDto.getDayNo())
                    .build();

            course.addItem(item);
        });

        courseRepository.save(course);
        return mapToCourseResDto(course);
    }

    /** 코스 상세 조회 */
    @Transactional(readOnly = true)
    public CourseResDto getCourseDetail(Long courseId) {
        Course course = courseRepository.findByIdWithItemsAndCategory(courseId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID(" + courseId + ")의 코스를 찾을 수 없습니다."));

        if ("N".equals(course.getStatus())) {
            throw new EntityNotFoundException("삭제된 코스입니다.");
        }

        return mapToCourseResDto(course);
    }

    /** 공개 코스 목록 */
    @Transactional(readOnly = true) // flush 동작을 생략 -> 조회 성능 향상을 위해 추가
    public Page<CourseResDto> getPublicCourses(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return courseRepository.findByStatus("Y", pageable)
                .map(this::mapToCourseResDto);
    }

    /** 아이템 추가 */
    public CourseItemResDto addCourseItem(Long courseId, CourseItemReqDto dto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("코스를 찾을 수 없습니다. id=" + courseId));

        Category category = categoryRepository.findById(dto.getCategoryCode())
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다. code=" + dto.getCategoryCode()));

        CourseItem item = CourseItem.builder()
                .course(course)
                .category(category)
                .placeId(dto.getPlaceId())
                .orderNo(dto.getOrderNo())
                .dayNo(dto.getDayNo())
                .build();

        itemRepository.save(item);
        return mapToItemResDto(item);

    }

    /** 아이템 순서 일괄 수정 */
    @Transactional
    public void updateItemsOrder(Long courseId, List<CourseOrderUpdateReqDto.OrderUpdateItem> items) {

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("수정할 아이템 목록이 비어 있습니다.");
        }

        // 프론트가 같은 dayNo의 아이템들만 보내므로, 대표 dayNo를 한 번 가져옴
        Integer dayNo = items.get(0).getDayNo();

        // 해당 코스, 해당 일차의 기존 아이템들 조회
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("코스를 찾을 수 없습니다. id=" + courseId));

        List<CourseItem> courseItems = itemRepository.findByCourseAndDayNoOrderByOrderNoAsc(course, dayNo);

        // 들어온 요청을 기준으로 orderNo 갱신
        for (CourseOrderUpdateReqDto.OrderUpdateItem orderDto : items) {
            courseItems.stream()
                    .filter(i -> i.getItemId().equals(orderDto.getItemId()))
                    .findFirst()
                    .ifPresent(i -> i.setOrderNo(orderDto.getOrderNo()));
        }

        itemRepository.saveAll(courseItems);
    }

    /** 코스 하루별 조회 */
    @Transactional(readOnly = true)
    public List<CourseItemResDto> getCourseItemsByDay(Long courseId, Integer dayNo) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID(" + courseId + ")의 코스를 찾을 수 없습니다."));

        List<CourseItem> items = itemRepository.findByCourseAndDayNoOrderByOrderNoAsc(course, dayNo);

        return items.stream()
                .map(this::mapToItemResDto)
                .toList();
    }


    /** 특정 코스의 아이템 삭제 */
    @Transactional
    public void deleteCourseItem(Long courseId, Long itemId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("코스를 찾을 수 없습니다. id=" + courseId));

        CourseItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("아이템을 찾을 수 없습니다. id=" + itemId));

        // 코스 소유 검증 (보안 차원)
        if (!item.getCourse().getCourseId().equals(courseId)) {
            throw new IllegalArgumentException("해당 코스의 아이템이 아닙니다.");
        }

        itemRepository.delete(item); // 물리 삭제
    }


    /** 코스 삭제 (Soft Delete) */
    @Transactional
    public void deleteCourse(Long courseId, String loginUserId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("코스를 찾을 수 없습니다. id=" + courseId));

        // 🔹 Soft Delete 권한 검증 (loginId 기준 비교)
        if (course.getCreator() == null || course.getCreator().getLoginId() == null) {
            throw new SecurityException("이 코스의 생성자 정보를 확인할 수 없습니다.");
        }

        if (!course.getCreator().getLoginId().equals(loginUserId)) {
            throw new SecurityException("본인이 생성한 코스만 삭제할 수 있습니다.");
        }

        course.setStatus("N"); // Soft Delete
    }


    /** 추천순 조회 */
    @Transactional(readOnly = true)
    public Page<CourseResDto> getPopularCoursesForPlan(Long planId, int page, int size) {

        TripPlan plan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("계획을 찾을 수 없습니다."));

        TourSpot startSpot = tspRepository
                .findByMapxAndMapy(plan.getStartMapX(), plan.getStartMapY())
                .orElseThrow(() -> new EntityNotFoundException("출발 관광지를 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(page, size);

        Page<Course> courses =
                courseRepository.findCoursesByStartSpotOrderByPopular(startSpot.getId(), pageable);

        return courses.map(this::mapToCourseResDto);
    }


    /** 코스 전체 수정 */
    @Transactional
    public CourseResDto updateCourse(Long planId, CourseReqDto dto) {

        TripPlan plan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("TripPlan을 찾을 수 없습니다. id=" + planId));

        Course course = plan.getCourse();
        if (course == null) {
            throw new IllegalStateException("해당 계획에 연결된 코스가 없습니다.");
        }

        // 기존 아이템 삭제
        course.getItems().clear();

        // 새 아이템 추가
        dto.getItems().forEach(itemDto -> {
            Category category = categoryRepository.findById(itemDto.getCategoryCode())
                    .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다. code=" + itemDto.getCategoryCode()));

            CourseItem item = CourseItem.builder()
                    .course(course)
                    .category(category)
                    .placeId(itemDto.getPlaceId())
                    .orderNo(itemDto.getOrderNo())
                    .dayNo(itemDto.getDayNo())
                    .build();

            course.addItem(item);
        });

        courseRepository.save(course);

        // 출발지 자동 업데이트
        updatePlanStartLocationByCourse(plan, course);

        // 코스 dayNo 기반 TripPlan.endDate 자동 조정
        int maxDay = course.getItems().stream()
                .mapToInt(CourseItem::getDayNo)
                .max()
                .orElse(1);

        LocalDate newEndDate = plan.getStartDate().plusDays(maxDay - 1);
        plan.setEndDate(newEndDate);

        tripPlanRepository.save(plan);  // Plan 변경 저장

        return mapToCourseResDto(course);
    }

    private void updatePlanStartLocationByCourse(TripPlan plan, Course course) {

        // 1일차 + orderNo 1 찾기
        CourseItem first = course.getItems().stream()
                .filter(i -> i.getDayNo() == 1)
                .sorted((a, b) -> a.getOrderNo() - b.getOrderNo())
                .findFirst()
                .orElse(null);

        if (first == null) return;

        String cat = first.getCategory().getCatCode();

        if ("tsp".equals(cat)) {
            tspRepository.findById(first.getPlaceId()).ifPresent(spot -> {
                plan.setStartLocation(spot.getTitle());
                plan.setStartMapX(spot.getMapx());
                plan.setStartMapY(spot.getMapy());
            });
        } else if ("acc".equals(cat)) {
            accRepository.findById(first.getPlaceId()).ifPresent(acc -> {
                plan.setStartLocation(acc.getTitle());
                plan.setStartMapX(acc.getMapx());
                plan.setStartMapY(acc.getMapy());
            });
        }

        tripPlanRepository.save(plan);
    }


    /** DTO 변환 */
    private CourseResDto mapToCourseResDto(Course course) {
//        Long planId = course.getPlans().stream()
//                .findFirst()
//                .map(p -> p.getId())
//                .orElse(null);



        return CourseResDto.builder()
                .courseId(course.getCourseId())
                .status(course.getStatus())
                .recCount(course.getRecCount())
                .createdAt(course.getCreatedAt())
                .creatorUserId(course.getCreator() != null ? course.getCreator().getLoginId() : null)
                .items(course.getItems().stream()
                        .map(this::mapToItemResDto)
                        .toList())
                .build();
    }



    /** 코스 아이템 변환 (장소명 포함) */
    private CourseItemResDto mapToItemResDto(CourseItem item) {
        String placeTitle = resolvePlaceTitle(item.getCategory().getCatCode(), item.getPlaceId());
        String imageUrl = resolvePlaceImage(item.getCategory().getCatCode(), item.getPlaceId());

        return CourseItemResDto.builder()
                .itemId(item.getItemId())
                .categoryCode(item.getCategory().getCatCode())
                .categoryName(item.getCategory().getCatName())
                .placeId(item.getPlaceId())
                .placeTitle(placeTitle) // 추가
                .placeImageUrl(imageUrl)   // 추가
                .orderNo(item.getOrderNo())
                .dayNo(item.getDayNo())
                .build();
    }

    private String resolvePlaceImage(String catCode, Long placeId) {
        if ("tsp".equals(catCode)) {
            return tspRepository.findById(placeId)
                    .map(TourSpot::getFirstImage)
                    .orElse(null);
        } else if ("acc".equals(catCode)) {
            return accRepository.findById(placeId)
                    .map(Acc::getFirstImage)
                    .orElse(null);
        }
        return null;
    }

    /** 장소명 찾기 로직 */
    private String resolvePlaceTitle(String catCode, Long placeId) {
        if (catCode == null || placeId == null) return "알 수 없음";

        switch (catCode) {
            case "tsp" -> { // 관광지
                Optional<TourSpot> tspOpt = tspRepository.findById(placeId);
                return tspOpt.map(TourSpot::getTitle).orElse("관광지 정보 없음");
            }
            case "acc" -> { // 숙소
                Optional<Acc> accOpt = accRepository.findById(placeId);
                return accOpt.map(Acc::getTitle).orElse("숙소 정보 없음");
            }
            default -> {
                return "기타 장소";
            }
        }
    }

    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID(" + courseId + ")의 코스를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Page<CourseResDto> getCoursesForPlan(Long planId, int page, int size) {

        TripPlan plan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("계획을 찾을 수 없습니다."));

        // 1) 출발 관광지를 좌표로 역검색
        TourSpot startSpot = tspRepository
                .findByMapxAndMapy(plan.getStartMapX(), plan.getStartMapY())
                .orElseThrow(() -> new EntityNotFoundException("출발 관광지를 찾을 수 없습니다."));

        Long startSpotId = startSpot.getId();

        Pageable pageable = PageRequest.of(page, size);

        // 2) 그 관광지가 포함된 코스만 조회
        Page<Course> courses = courseRepository.findCoursesByStartSpot(startSpotId, pageable);

        return courses.map(this::mapToCourseResDto);
    }

}