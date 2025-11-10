package com.multi.travel.course.repository;

import com.multi.travel.course.entity.CourseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : lsa03
 * @filename : CourseItemRepository
 * @since : 2025-11-08 토요일
 */
public interface CourseItemRepository extends JpaRepository<CourseItem, Long> {
}
