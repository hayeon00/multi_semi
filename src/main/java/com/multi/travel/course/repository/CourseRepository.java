package com.multi.travel.course.repository;

import com.multi.travel.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : CourseRepository
 * @since : 2025-11-08 토요일
 */
public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByStatus(String status, Pageable pageable);
}
