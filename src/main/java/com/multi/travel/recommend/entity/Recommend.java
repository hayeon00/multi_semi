package com.multi.travel.recommend.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : Recommend
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 11. 화요일
 */

import com.multi.travel.category.entity.Category;
import com.multi.travel.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_rec", uniqueConstraints = @UniqueConstraint(columnNames = {"target_id", "user_id", "cat_code"}))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recommend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "status", columnDefinition = "CHAR(1) CHECK (status IN ('Y','N')) DEFAULT 'Y'")
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cat_code")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;


    public void toggleStatus(){
        if(status.equals("Y")){
            status = "N";
        }else {
            status = "Y";
        }
    }

}
