package com.multi.travel.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiExceptionDto> exceptionHandler(Exception e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500
                .body(new ApiExceptionDto(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ApiExceptionDto> exceptionHandler(RefreshTokenException e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiExceptionDto(HttpStatus.UNAUTHORIZED, e.getMessage()));
    }

    @ExceptionHandler(TourSpotNotFoundException.class)
    public ResponseEntity<?> handleTourSpotNotFound(TourSpotNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(AccommodationNotFound.class)
    public ResponseEntity<?> handleAccommodationNotFound(AccommodationNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<?> handleAToken(AccommodationNotFound ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }

//    @ExceptionHandler(Exception.class)
//    public Object  exceptionHandler(Exception e, HandlerMethod handler, Model model) {
//        e.printStackTrace();
//
//        boolean isRestController = handler.getBeanType().isAnnotationPresent(RestController.class);//해당 컨트롤러 클래스에 @RestController가 붙었는지를 판별
//        if (isRestController) {
//            // REST API 요청 → JSON 반환
//        return ResponseEntity
//                .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500
//                .body(new ApiExceptionDto(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
//        } else {
//            // 일반 요청 → HTML 페이지 렌더링
//            model.addAttribute("errorMessage", e.getMessage());
//            return "error-page";
//        }
//    }

}