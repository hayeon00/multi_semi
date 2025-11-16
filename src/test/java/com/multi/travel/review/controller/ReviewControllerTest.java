//package com.multi.travel.review.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.multi.travel.review.dto.*;
//import com.multi.travel.review.service.ReviewService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//// ReviewController만 로드하는 통합 테스트
//@WebMvcTest(ReviewController.class)
//class ReviewControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private ReviewService reviewService;
//
//    private final String TEST_USER_ID = "testUser";
//    private ReviewDetailDto mockReviewDetail;
//
//    @BeforeEach
//    void setUp() {
//        mockReviewDetail = ReviewDetailDto.builder()
//                .reviewId(1L).title("테스트 리뷰 제목").rating(5).writer(TEST_USER_ID).createdAt(LocalDateTime.now())
//                .targetType("tsp").targetId(100L).planId(10L).imageUrls(List.of("url1.jpg")).build();
//    }
//
//    // =========================================================
//    // 1. 단일 리뷰 등록 (POST /reviews) - @ModelAttribute & @RequestParam(images) 검증
//    // =========================================================
//    @Test
//    @DisplayName("POST /reviews: 단일 리뷰 등록 요청 시 200 OK와 DTO를 반환한다")
//    @WithMockUser(username = TEST_USER_ID) // 인증된 사용자 Mocking
//    void createReview_Success() throws Exception {
//        // Given
//        when(reviewService.createReview(any(ReviewReqDto.class), anyList(), eq(TEST_USER_ID))).thenReturn(mockReviewDetail);
//
//        MockMultipartFile imageFile = new MockMultipartFile("images", "test.jpg", "image/jpeg", "image content".getBytes());
//
//        // When & Then
//        mockMvc.perform(MockMvcRequestBuilders.multipart("/reviews")
//                        .file(imageFile)
//                        // ReviewReqDto의 필드들을 param으로 전달
//                        .param("tripPlanId", "10")
//                        .param("targetType", "tsp")
//                        .param("targetId", "100")
//                        .param("title", "제목")
//                        .param("rating", "5")
//                        .with(csrf())) // POST/PUT/DELETE 요청에 필수
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.reviewId").value(1L));
//
//        verify(reviewService).createReview(any(ReviewReqDto.class), anyList(), eq(TEST_USER_ID));
//    }
//
//
//    // =========================================================
//    // 2. 복합 리뷰 등록 (POST /reviews/complex) - DTO JSON part 검증
//    // =========================================================
//    @Test
//    @DisplayName("POST /reviews/complex: 복합 리뷰 등록 요청 시 200 OK를 반환한다")
//    @WithMockUser(username = TEST_USER_ID)
//    void createComplexReview_Success() throws Exception {
//        // Given
//        MainReviewDto mainReviewDto = MainReviewDto.builder().title("메인 리뷰").build();
//        ComplexReviewReqDto reqDto = new ComplexReviewReqDto(mainReviewDto, List.of(), null);
//        String jsonDto = objectMapper.writeValueAsString(reqDto);
//
//        // dto 파트: application/json
//        MockMultipartFile dtoPart = new MockMultipartFile("dto", "", "application/json", jsonDto.getBytes());
//        // images 파트: image/jpeg
//        MockMultipartFile imagePart = new MockMultipartFile("images", "new.jpg", "image/jpeg", "image content".getBytes());
//
//        // When & Then
//        mockMvc.perform(MockMvcRequestBuilders.multipart("/reviews/complex")
//                        .file(dtoPart)
//                        .file(imagePart)
//                        .with(csrf()))
//                .andExpect(status().isOk());
//
//        verify(reviewService).createComplexReview(any(ComplexReviewReqDto.class), anyList(), eq(TEST_USER_ID));
//    }
//
//    // =========================================================
//    // 3. 복합 리뷰 수정 데이터 로딩 (GET /reviews/plan/{planId}/complex-edit) - DTO 변환 로직 검증
//    // =========================================================
//    @Test
//    @DisplayName("GET /reviews/plan/{planId}/complex-edit: 메인/스팟 리뷰 데이터 구조 변환을 검증한다")
//    @WithMockUser(username = TEST_USER_ID)
//    void getComplexReviewForEdit_Success_StructureCheck() throws Exception {
//        // Given
//        Long planId = 10L;
//        ReviewDetailDto mainReviewDto = ReviewDetailDto.builder().reviewId(1L).targetType("course").title("제주도 2박 3일").build();
//        ReviewDetailDto spotReviewDto = ReviewDetailDto.builder().reviewId(2L).targetType("tsp").title("관광지 A").build();
//
//        // Service는 ReviewDetailDto 목록을 반환
//        when(reviewService.getAllReviewsByPlanForEdit(eq(planId), eq(TEST_USER_ID)))
//                .thenReturn(List.of(mainReviewDto, spotReviewDto));
//
//        // When & Then
//        mockMvc.perform(MockMvcRequestBuilders.get("/reviews/plan/{planId}/complex-edit", planId)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.mainReview.reviewId").value(1L))
//                .andExpect(jsonPath("$.courseTitle").value("제주도 2박 3일"))
//                // Controller에서 ReviewDetailDto의 title이 SpotReviewDto의 placeTitle로 올바르게 매핑되었는지 검증
//                .andExpect(jsonPath("$.spotReviews[0].reviewId").value(2L))
//                .andExpect(jsonPath("$.spotReviews[0].placeTitle").value("관광지 A"));
//    }
//}