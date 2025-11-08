package com.multi.travel.course.entity;

import com.multi.travel.mock.entity.Plan;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : lsa03
 * @filename : Course
 * @since : 2025-11-08 토요일
 */
@Entity
@Table(name = "tb_crs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    /** 계획(Plan) 참조 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "status", length = 1)
    private String status = "Y";  // Y: 활성, N: 삭제

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt = LocalDateTime.now();

    /** 코스 아이템 리스트 (1:N) */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseItem> items = new ArrayList<>();

    /** 편의 메서드 */
    public void addItem(CourseItem item) {
        items.add(item);
        item.setCourse(this);
    }

}
