package com.multi.travel.common.exception;

/*
 * Please explain the class!!!
 *
 * @filename    : TourSpotNotFoundException
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 9. 일요일
 */


public class TourSpotNotFoundException extends RuntimeException {
    public TourSpotNotFoundException(Long id) {
        super("해당 관광지를 찾을 수 없습니다. id=" + id);
    }
}
