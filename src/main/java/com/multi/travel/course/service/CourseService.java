package com.multi.travel.course.service;

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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("코스를 찾을 수 없습니다. id=" + courseId));

        course.setStatus("N"); // Soft Delete 처리
    }


    /** 추천순 조회 */
    @Transactional(readOnly = true)
    public List<CourseResDto> getPopularCourses(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("recCount").descending());
        Page<Course> courses = courseRepository.findByStatus("Y", pageable);

        return courses.stream()
                .map(this::mapToCourseResDto)
                .toList();
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

        // 기존 아이템 전체 삭제 (고아 제거 활성화되어 있음)
        course.getItems().clear();

        // 수정된 아이템 추가
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

        // 계획 기본정보도 함께 수정
        plan.setTitle(dto.getMemberId());  // (승아님 상황에 맞게 수정 필요)
        plan.setNumberOfPeople(plan.getNumberOfPeople());
        plan.setStartDate(plan.getStartDate());
        plan.setEndDate(plan.getEndDate());
        tripPlanRepository.save(plan);

        courseRepository.save(course);

        return mapToCourseResDto(course);
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
                .items(course.getItems().stream()
                        .map(this::mapToItemResDto)
                        .toList())
                .build();
    }



    private CourseItemResDto mapToItemResDto(CourseItem item) {
        return CourseItemResDto.builder()
                .itemId(item.getItemId())
                .categoryCode(item.getCategory().getCatCode())
                .categoryName(item.getCategory().getCatName())
                .placeId(item.getPlaceId())
                .orderNo(item.getOrderNo())
                .dayNo(item.getDayNo())
                .build();
    }

    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID(" + courseId + ")의 코스를 찾을 수 없습니다."));
    }

}