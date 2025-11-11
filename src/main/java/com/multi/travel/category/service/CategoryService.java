package com.multi.travel.category.service;

/*
 * Please explain the class!!!
 *
 * @filename    : CategoryService
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 10. 월요일
 */


import com.multi.travel.category.CategoryRepository;
import com.multi.travel.category.dto.CategoryDTO;
import com.multi.travel.category.entity.Category;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryDTO findById(String cateCode) {
        Category category = categoryRepository.findById(cateCode)
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다. cateCode=" + cateCode));
        return CategoryDTO.builder()
                .cateCode(category.getCatCode())
                .cateName(category.getCatName())
                .build();
    }


}
