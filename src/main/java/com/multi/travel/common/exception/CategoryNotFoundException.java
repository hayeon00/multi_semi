package com.multi.travel.common.exception;

/*
 * Please explain the class!!!
 *
 * @filename    : CategoryNotFoundException
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 11. 화요일
 */


public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String catCode) {
        super("해당 카테고리를 찾을 수 없습니다. catCode="+catCode);
    }
}
