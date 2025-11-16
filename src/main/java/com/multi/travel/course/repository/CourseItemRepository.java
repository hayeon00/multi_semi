package com.multi.travel.course.repository;

import com.multi.travel.course.entity.Course;
import com.multi.travel.course.entity.CourseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : CourseItemRepository
 * @since : 2025-11-08 토요일
 */
public interface CourseItemRepository extends JpaRepository<CourseItem, Long> {
    // 하루별 코스 조회용 쿼리 메서드
    List<CourseItem> findByCourseAndDayNoOrderByOrderNoAsc(Course course, Integer dayNo);

    List<CourseItem> findByCourse(Course course);

    //List<CourseItem> findByCourseId(void attr0);
}
