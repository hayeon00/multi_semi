package com.multi.travel.course.service;

import com.multi.travel.course.dto.*;
import com.multi.travel.course.entity.Course;
import com.multi.travel.course.entity.CourseItem;
import com.multi.travel.course.repository.CourseItemRepository;
import com.multi.travel.course.repository.CourseRepository;
import com.multi.travel.member.entity.Member;
import com.multi.travel.plan.entity.TripPlan;
import com.multi.travel.plan.repository.TripPlanRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.ManyToOne;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
            CourseItem item = CourseItem.builder()
                    .course(course)
                    .placeType(itemDto.getPlaceType())
                    .placeId(itemDto.getPlaceId())
                    .orderNo(itemDto.getOrderNo())
                    .build();
            course.addItem(item);
        });

        courseRepository.save(course);
        return mapToCourseResDto(course);
    }

    /** 코스 상세 조회 */
    @Transactional(readOnly = true) // flush 동작을 생략 -> 조회 성능 향상을 위해 추가
    public CourseResDto getCourseDetail(Long courseId) {
        Course course = courseRepository.findById(courseId)
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
                .orElseThrow(() -> new EntityNotFoundException("해당 ID(" + courseId + ")의 코스를 찾을 수 없습니다."));

        CourseItem item = CourseItem.builder()
                .course(course)
                .placeType(dto.getPlaceType())
                .placeId(dto.getPlaceId())
                .orderNo(dto.getOrderNo())
                .build();

        itemRepository.save(item);
        return mapToItemResDto(item);
    }

    /** 아이템 순서 일괄 수정 */
    public void updateItemsOrder(Long courseId, List<CourseOrderUpdateReqDto.OrderUpdateItem> items) {
        for (CourseOrderUpdateReqDto.OrderUpdateItem order : items) {
            CourseItem item = itemRepository.findById(order.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("해당 ID(" + order.getItemId() + ")의 장소를 찾을 수 없습니다."));

            item.setOrderNo(order.getOrderNo());
            itemRepository.save(item);
        }
    }

    /** TODO: 특정 코스의 아이템 삭제 구현 필요 */

    /** TODO: 코스 삭제 (Soft Delete) 구현 필요 */

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
                .placeType(item.getPlaceType())
                .placeId(item.getPlaceId())
                .orderNo(item.getOrderNo())
                .build();
    }
}