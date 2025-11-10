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
    private String cat_code;

    @Column(length = 100)
    private String cat_name;
}