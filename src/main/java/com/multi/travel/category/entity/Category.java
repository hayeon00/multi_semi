package com.multi.travel.category.entity;
/*
 * Please explain the class!!!
 *
 * @filename    : Category
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 8. 토요일
 */

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "tb_cat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @Column(name = "cat_code")
    private String catCode;

    @Column(name = "cat_name", length = 100)
    private String catName;
}