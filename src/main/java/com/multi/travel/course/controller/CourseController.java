package com.multi.travel.course.controller;

import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.common.ResponseDto;
import com.multi.travel.course.dto.*;
import com.multi.travel.course.service.CourseService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : CourseController
 * @since : 2025-11-08 토요일
 */
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /** 코스 생성 */
    @PostMapping
    public ResponseEntity<ResponseDto> createCourse(@RequestBody CourseReqDto dto) {
        //        return ResponseEntity.status(HttpStatus.CREATED)
        //                .body(courseService.createCourse(dto));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(HttpStatus.CREATED, "코스 생성 완료", courseService.createCourse(dto)));
    }


    /** 코스 상세 조회 */
    @GetMapping("/{courseId}")
    public ResponseEntity<ResponseDto> getCourseDetail(@PathVariable Long courseId) {
//        return ResponseEntity.ok(courseService.getCourseDetail(courseId));
        CourseResDto response = courseService.getCourseDetail(courseId);
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "코스 상세 조회 성공", response)
        );

    }

    /** 코스 목록 조회 */
    @GetMapping
    public ResponseEntity<ResponseDto> getPublicCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
//        return ResponseEntity.ok(courseService.getPublicCourses(page, size));
        Page<CourseResDto> courses = courseService.getPublicCourses(page, size);

        if (courses.isEmpty()) {
            return ResponseEntity.ok(
                    new ResponseDto(HttpStatus.OK, "조회된 코스가 없습니다.", courses)
            );
        }

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "코스 목록 조회 성공", courses)
        );

    }

    /** 아이템 추가 */
    @PostMapping("/{courseId}/items")
    public ResponseEntity<ResponseDto> addCourseItem(
            @PathVariable Long courseId,
            @RequestBody CourseItemReqDto dto) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(courseService.addCourseItem(courseId, dto));
        CourseItemResDto response = courseService.addCourseItem(courseId, dto);
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.CREATED, "코스 아이템 추가 성공", response)
        );
    }

    /** 아이템 순서 일괄 수정 */
    @PutMapping("/{courseId}/items/order")
    public ResponseEntity<Void> updateItemsOrder(
            @PathVariable Long courseId,
            @RequestBody CourseOrderUpdateReqDto dto) {
        courseService.updateItemsOrder(courseId, dto.getItems());
        return ResponseEntity.noContent().build();
    }

    /** 하루별 코스 아이템 조회 */
    @GetMapping("/{courseId}/items")
    public ResponseEntity<ResponseDto> getCourseItemsByDay(
            @PathVariable Long courseId,
            @RequestParam("day") Integer dayNo
    ) {
        List<CourseItemResDto> items = courseService.getCourseItemsByDay(courseId, dayNo);

        if (items.isEmpty()) {
            return ResponseEntity.ok(
                    new ResponseDto(HttpStatus.OK, dayNo + "일차 코스가 비어 있습니다.", items)
            );
        }

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, dayNo + "일차 코스 조회 성공", items)
        );
    }


    /** 코스 아이템 삭제 */
    @DeleteMapping("/{courseId}/items/{itemId}")
    public ResponseEntity<ResponseDto> deleteCourseItem(
            @PathVariable Long courseId,
            @PathVariable Long itemId
    ) {
        courseService.deleteCourseItem(courseId, itemId);
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "코스 아이템 삭제 성공", null)
        );
    }

    /** 코스 삭제 (Soft Delete - 생성자 본인만 가능) */
    @DeleteMapping("/{courseId}")
    public ResponseEntity<ResponseDto> deleteCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUser loginUser
    ) {
        try {
            courseService.deleteCourse(courseId, loginUser.getUserId());
            return ResponseEntity.ok(
                    new ResponseDto(HttpStatus.OK, "코스 삭제(비활성화) 완료", null)
            );
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDto(HttpStatus.FORBIDDEN, e.getMessage(), null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDto(HttpStatus.NOT_FOUND, e.getMessage(), null));
        }
    }

    /** 추천순 코스 목록 조회 */
    @GetMapping("/filter/popular")
    public ResponseEntity<ResponseDto> getPopularCoursesForPlan(
            @RequestParam Long planId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        return ResponseEntity.ok(new ResponseDto(
                HttpStatus.OK,
                "필터 + 추천순 코스 조회 성공",
                courseService.getPopularCoursesForPlan(planId, page, size)
        ));
    }


    /** 코스 전체 수정 (기존 아이템 삭제 후 재등록) */
    @PutMapping("/{planId}")
    public ResponseEntity<ResponseDto> updateCourse(
            @PathVariable Long planId,
            @Valid @RequestBody CourseReqDto dto
    ) {
        CourseResDto updated = courseService.updateCourse(planId, dto);
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "코스 수정 완료", updated)
        );
    }

    /** 계획 기반 코스 검색 */
    @GetMapping("/filter")
    public ResponseEntity<ResponseDto> getCoursesForPlan(
            @RequestParam Long planId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        Page<CourseResDto> result = courseService.getCoursesForPlan(planId, page, size);
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "계획 기반 코스 목록 조회 성공", result));
    }


}