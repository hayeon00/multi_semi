package com.multi.travel.course.service;

import com.multi.travel.course.repository.CourseRepository;
import com.multi.travel.plan.entity.TripPlan;
import com.multi.travel.plan.repository.TripPlanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.when;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : CourseServiceTest
 * @since : 2025-11-10 월요일
 */

@SpringBootTest
class CourseServiceTest {
    /** DB에 실제로 접근하지 않고 로직이 정상적으로 작동하는지 검증하기 */

    @MockitoBean
    private TripPlanRepository tripPlanRepository;

    @MockitoBean
    private CourseRepository courseRepository;

    @Autowired
    private CourseService courseService;

    @Test
    @DisplayName("코스 수동 생성 성공 테스트")
    void createCourse_success() {
        // given
        Long planId = 1L;
        TripPlan mockPlan = TripPlan.builder()
                .id(planId)
                .title("제주 여행")
                .build();

        when(tripPlanRepository.findById(planId))
                .thenReturn(Optional.of(mockPlan));

        // 요청 DTO
//        CourseItemReqDto item1 = new CourseItemReqDto("TOUR_SPOT", 1L, 1);
//        CourseItemReqDto item2 = new CourseItemReqDto("ACCOMMODATION", 2L, 2);
//        CourseReqDto dto = new CourseReqDto(planId, List.of(item1, item2));

        // when
//        CourseResDto result = courseService.createCourse(dto);

        // then
//        assertThat(result).isNotNull();
//        assertThat(result.getStatus()).isEqualTo("Y");
//        assertThat(result.getItems()).hasSize(2);

        // verify — repository 호출 검증
//        verify(tripPlanRepository).findById(planId);
//        verify(tripPlanRepository).save(any(TripPlan.class));
//        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("존재하지 않는 Plan ID면 예외 발생")
    void createCourse_fail_planNotFound() {
        // given
        when(tripPlanRepository.findById(anyLong())).thenReturn(Optional.empty());
//        CourseReqDto dto = new CourseReqDto(999L, List.of());

        // then
//        assertThatThrownBy(() -> courseService.createCourse(dto))
//                .isInstanceOf(EntityNotFoundException.class)
//                .hasMessageContaining("계획을 찾을 수 없습니다");

//        verify(courseRepository, never()).save(any());
    }

    @Test
    void getCourseDetail() {
    }

    @Test
    void getPublicCourses() {
    }

    @Test
    void addCourseItem() {
    }

    @Test
    void updateItemsOrder() {
    }
}