package com.multi.travel.course.entity;

import com.multi.travel.member.entity.Member;
import com.multi.travel.plan.entity.TripPlan;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
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

    /** 여러 Plan이 하나의 Course를 참조하므로, 역참조 가능하도록 설정 */
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    @Builder.Default // 추가 이유: Builder로 생성할 때도 초기값이 반영되도록
    private List<TripPlan> plans = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")   // 누가 만든 코스인지 저장
    private Member creator;

    @Column(name = "status", length = 1, nullable = false)
    @Builder.Default // 추가 이유: Builder로 생성할 때도 초기값이 반영되도록
    private String status = "Y";  // Y=활성화, N=사용자 기준에서 삭제 (소프트 삭제)

    /** 추천 수 (추후 Recommend 테이블과 연동 예정) */
    @Column(name = "rec_count", columnDefinition = "INT DEFAULT 0")
    @Builder.Default // 추가 이유: Builder로 생성할 때도 초기값이 반영되도록
    private Integer recCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    /** 코스 아이템 리스트 (1:N) */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default //@Builder.Default를 붙이지 않으면 Lombok의 Builder가 null로 초기화해버림
    private List<CourseItem> items = new ArrayList<>();

    public List<CourseItem> getCoursePlaces() {
        return this.items;
    }

    // cascade = CascadeType.ALL

    // orphanRemoval = true : 부모 엔티티에서 자식 엔티티의 참조가 끊어지면,
    //                        JPA가 그 자식 엔티티를 DB에서도 자동으로 삭제하겠다는 뜻.
    //                        -> 완전히 종속적인 자식일 경우 상용하는 것이 바람직 (공유 관계에서는 절대 사용 금지).
    // 즉, 부모-자식 연관관계에서 고아 객체(Orphan) 를 자동 정리하는 기능.
    //      - cascade = REMOVE: 부모 엔티티가 삭제될 때 자식도 같이 삭제됨
    //      - orphanRemoval = true: 부모 컬렉션에서 자식의 참조가 끊어졌을 때 자식만 삭제됨


    /** 편의 메서드 */
    public void addItem(CourseItem item) {
        items.add(item);
        item.setCourse(this);
    }


}
