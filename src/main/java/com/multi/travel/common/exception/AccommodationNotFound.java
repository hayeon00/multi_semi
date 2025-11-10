package com.multi.travel.common.exception;

/*
 * Please explain the class!!!
 *
 * @filename    : AccommodationNotFound
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 9. 일요일
 */


public class AccommodationNotFound extends RuntimeException {
    public AccommodationNotFound(Long id) {
        super("해당 숙소를 찾을 수 없습니다. id=" + id);
    }
}
