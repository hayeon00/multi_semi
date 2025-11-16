package com.multi.travel.review.service;

import com.multi.travel.common.file.FileService;
import com.multi.travel.member.entity.Member;
import com.multi.travel.member.repository.MemberRepository;
import com.multi.travel.plan.entity.TripPlan;
import com.multi.travel.plan.repository.TripPlanRepository;
import com.multi.travel.review.dto.ComplexReviewReqDto;
import com.multi.travel.review.dto.MainReviewDto;
import com.multi.travel.review.dto.ReviewReqDto;
import com.multi.travel.review.dto.SpotReviewDto;
import com.multi.travel.review.entity.Review;
import com.multi.travel.review.entity.ReviewImage;
import com.multi.travel.review.repository.ReviewImageRepository;
import com.multi.travel.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock private ReviewRepository reviewRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private FileService fileService;
    @Mock private ReviewImageRepository reviewImageRepository; // 복합 리뷰 수정 테스트에서 findByImageUrl 사용
    @Mock private TripPlanRepository tripPlanRepository;
    @Mock private MultipartFile mockImageFile;

    // 공통 상수 및 객체
    private final String TEST_USER_ID = "testUser";
    private final Long TEST_MEMBER_ID = 1L;
    private final Long TEST_PLAN_ID = 10L;
    private final Long TEST_REVIEW_ID = 1L;
    private final String IMAGE_URL_PREFIX = "http://test-url/";
    private Member mockMember;
    private TripPlan mockTripPlan;

    @BeforeEach
    void setUp() {
        // 1. @Value 주입: 모든 테스트에 공통으로 필요
        ReflectionTestUtils.setField(reviewService, "IMAGE_URL", IMAGE_URL_PREFIX);

        // 2. 공통 객체 초기화
        mockMember = Member.builder().id(TEST_MEMBER_ID).loginId(TEST_USER_ID).username("테스트사용자").build();
        mockTripPlan = TripPlan.builder().id(TEST_PLAN_ID).title("여행계획제목").build();
    }

    // ---------------------------------------------------------
    // 1. 리뷰 등록 (createReview)
    // ---------------------------------------------------------
    @Test
    @DisplayName("성공: 단일 리뷰 등록 시, 리뷰 및 이미지가 cascade로 저장됨")
    void createReview_Success() {
        // Given
        // * UnnecessaryStubbingException 방지를 위해 여기에 Mock 설정을 배치
        when(memberRepository.findByLoginId(TEST_USER_ID)).thenReturn(Optional.of(mockMember));
        when(tripPlanRepository.findById(TEST_PLAN_ID)).thenReturn(Optional.of(mockTripPlan));
        when(fileService.store(any(MultipartFile.class))).thenReturn("stored_img.jpg");
        when(reviewRepository.save(any(Review.class))).thenReturn(
                Review.builder().id(TEST_REVIEW_ID).member(mockMember).images(new ArrayList<>()).build()
        );

        ReviewReqDto reqDto = ReviewReqDto.builder().tripPlanId(TEST_PLAN_ID).title("단일 리뷰").build();

        // When
        reviewService.createReview(reqDto, List.of(mockImageFile), TEST_USER_ID);

        // Then
        // Review 엔티티 저장 검증 (Cascade로 이미지 저장도 함께 이루어짐)
        verify(reviewRepository, times(1)).save(any(Review.class));
        // 파일 저장 로직이 호출되었는지 검증
        verify(fileService, times(1)).store(any(MultipartFile.class));
    }

    // ---------------------------------------------------------
    // 2. 리뷰 삭제 (deleteReview)
    // ---------------------------------------------------------
    @Test
    @DisplayName("성공: 작성자 본인이 리뷰 삭제 요청 시, 리뷰 및 파일이 삭제된다")
    void deleteReview_Success() {
        // Given
        // * NullPointerException 방지를 위해 member를 확실하게 주입합니다.
        ReviewImage img1 = ReviewImage.builder().storedName("file1.jpg").build();
        Review reviewToDelete = Review.builder()
                .id(TEST_REVIEW_ID)
                .member(mockMember) // ⬅️ member 객체를 확실히 주입
                .images(List.of(img1))
                .build();

        when(reviewRepository.findById(TEST_REVIEW_ID)).thenReturn(Optional.of(reviewToDelete));

        // When
        reviewService.deleteReview(TEST_REVIEW_ID, TEST_USER_ID);

        // Then
        // 파일 서비스 삭제 및 리뷰 엔티티 삭제 검증
        verify(fileService, times(1)).delete("file1.jpg");
        verify(reviewRepository, times(1)).delete(reviewToDelete);
    }

    // ---------------------------------------------------------
    // 3. 복합 리뷰 수정 (updateComplexReview) - 핵심 로직 (삭제/생성)
    // ---------------------------------------------------------
    @Test
    @DisplayName("성공: 복합 리뷰 수정 시, 이미지 삭제 및 스팟 리뷰 삭제/생성 로직이 정상 동작한다 (간소화)")
    void updateComplexReview_Success_Simplified() {
        // Given
        // * 필요한 Mock 설정만 최소한으로 배치
        when(memberRepository.findByLoginId(TEST_USER_ID)).thenReturn(Optional.of(mockMember));

        // 1. 기존 메인 리뷰 Mock 및 이미지 연결
        Review mainReview = Review.builder().id(TEST_REVIEW_ID).member(mockMember).images(new ArrayList<>()).build();
        when(reviewRepository.findById(TEST_REVIEW_ID)).thenReturn(Optional.of(mainReview));

        // 2. 삭제할 이미지 Mock (서비스는 findByImageUrl을 호출)
        String deletedUrl = IMAGE_URL_PREFIX + "deleted.jpg";
        ReviewImage deletedImage = ReviewImage.builder().storedName("deleted.jpg").imageUrl(deletedUrl).review(mainReview).build();
        mainReview.getImages().add(deletedImage);
        when(reviewImageRepository.findByImageUrl(deletedUrl)).thenReturn(Optional.of(deletedImage));

        // 3. 스팟 리뷰 삭제/생성 DTO 설정
        SpotReviewDto spotDtoDelete = SpotReviewDto.builder().reviewId(3L).content(null).rating(0).build(); // 삭제 의도
        SpotReviewDto spotDtoNew = SpotReviewDto.builder().reviewId(null).targetType("acc").content("새 스팟").rating(5).build(); // 신규 생성 의도

        MainReviewDto mainDto = MainReviewDto.builder().reviewId(TEST_REVIEW_ID).title("수정된 제목").content("수정 내용").build();

        ComplexReviewReqDto reqDto = new ComplexReviewReqDto(mainDto, List.of(spotDtoDelete, spotDtoNew), List.of(deletedUrl));
        List<MultipartFile> newImages = List.of(mockImageFile);

        // 4. 스팟 리뷰 Mock
        Review spotReviewToDelete = Review.builder().id(3L).member(mockMember).build();
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(spotReviewToDelete));

        // 5. 새 이미지/리뷰 저장 Mock
        when(fileService.store(any(MultipartFile.class))).thenReturn("new_stored.jpg");
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        reviewService.updateComplexReview(TEST_REVIEW_ID, reqDto, newImages, TEST_USER_ID);

        // Then
        // 1. 이미지 삭제 검증
        verify(fileService, times(1)).delete("deleted.jpg");

        // 2. 새 이미지 추가 검증
        verify(fileService, times(1)).store(any(MultipartFile.class));

        // 3. 스팟 리뷰 처리 검증
        verify(reviewRepository, times(1)).delete(spotReviewToDelete); // 삭제 (spotDtoDelete)
        verify(reviewRepository, times(1)).save(any(Review.class)); // 새 리뷰 (spotDtoNew) 저장
    }
}