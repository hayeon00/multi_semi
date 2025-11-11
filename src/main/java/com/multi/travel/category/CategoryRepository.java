package com.multi.travel.category;

/*
 * Please explain the class!!!
 *
 * @filename    : CategoryRepository
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 10. 월요일
 */


import com.multi.travel.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {

}
