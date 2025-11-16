package com.multi.travel.course.repository;

import com.multi.travel.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : CourseRepository
 * @since : 2025-11-08 토요일
 */
public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByStatus(String status, Pageable pageable);

    // 추천순 정렬 조회
    @Query("""
    SELECT DISTINCT c FROM Course c
    JOIN c.items i
    WHERE i.placeId = :spotId
      AND c.status = 'Y'
    ORDER BY c.recCount DESC, c.createdAt DESC
""")
    Page<Course> findCoursesByStartSpotOrderByPopular(@Param("spotId") Long spotId, Pageable pageable);

    @Query("SELECT DISTINCT c FROM Course c " +
            "LEFT JOIN FETCH c.items i " +
            "LEFT JOIN FETCH i.category " +
            "WHERE c.courseId = :courseId")
    Optional<Course> findByIdWithItemsAndCategory(@Param("courseId") Long courseId);


    @Query("""
    SELECT DISTINCT c FROM Course c
    JOIN c.items i
    WHERE i.placeId = :spotId
      AND c.status = 'Y'
""")
    Page<Course> findCoursesByStartSpot(@Param("spotId") Long spotId, Pageable pageable);
}
