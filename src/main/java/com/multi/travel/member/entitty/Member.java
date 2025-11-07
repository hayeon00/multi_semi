package com.multi.travel.member.entitty;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : chang
 * @filename : Member
 * @since : 2025-11-07 금요일
 */

@Entity
@Table(name = "member")
/* 매핑될 테이블의 이름을 작성한다.
 * 생략하면 자동으로 클래스 이름을 테이블의 이름으로 사용한다.
 * */
@NoArgsConstructor
@AllArgsConstructor
@Getter            //Data로 절대 넣지말것
@Builder
public class Member {




}
