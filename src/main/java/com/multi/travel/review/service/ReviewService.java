package com.multi.travel.review.service;

import com.multi.travel.admin.repository.TourSpotRepository;
import com.multi.travel.common.file.FileService;
import com.multi.travel.course.entity.Course;
import com.multi.travel.course.entity.CourseItem;
import com.multi.travel.course.repository.CourseItemRepository;
import com.multi.travel.course.repository.CourseRepository;
import com.multi.travel.member.entity.Member;
import com.multi.travel.member.repository.MemberRepository;
import com.multi.travel.plan.entity.TripPlan;
import com.multi.travel.plan.repository.TripPlanRepository;
import com.multi.travel.review.dto.*;
import com.multi.travel.review.entity.Review;
import com.multi.travel.review.entity.ReviewImage;
import com.multi.travel.review.repository.ReviewImageRepository;
import com.multi.travel.review.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {

    @Value("${image.review.image-dir}")
    private String IMAGE_DIR;

    @Value("${image.review.image-url}")
    private String IMAGE_URL;

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final MemberRepository memberRepository;
    private final TourSpotRepository tourSpotRepository;
    private final FileService fileService;
    private final TripPlanRepository tripPlanRepository;
    private final CourseRepository courseRepository;
    private final CourseItemRepository courseItemRepository;


    public ReviewDetailDto createReview(ReviewReqDto dto, List<MultipartFile> images, String userId) {
        log.debug("🧪 createReview() 호출됨 - 전달된 userId: {}", userId);

        Member member = memberRepository.findByLoginId(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        Review review = Review.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .rating(dto.getRating())
                .targetType(dto.getTargetType())
                .targetId(dto.getTargetId())
                .member(member)
                .build();

        // 이미지 저장 처리
        if (images != null && !images.isEmpty()) {
            log.debug("📷 이미지 수: {}", images.size());

            for (MultipartFile file : images) {
                String storedName = fileService.store(file);
                ReviewImage image = ReviewImage.builder()
                        .originalName(file.getOriginalFilename())
                        .storedName(storedName)
                        .imageUrl(IMAGE_URL + storedName)
                        .build();

                review.addImage(image);
            }
        }


        Review saved = reviewRepository.save(review);
        return toDto(saved);
    }


    public ReviewDetailDto updateReview(Long reviewId, ReviewReqDto dto, List<MultipartFile> newImages, String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        if (!review.getMember().getLoginId().equals(userId)) {
            throw new SecurityException("본인 리뷰만 수정할 수 있습니다.");
        }

        //기존 이미지 삭제
        for (ReviewImage img : review.getImages()) {
            fileService.delete(img.getStoredName());
        }
        review.getImages().clear();

        //새이미지 등록
        if (newImages != null && !newImages.isEmpty()) {
            for (MultipartFile file : newImages) {
                String storedName = fileService.store(file);
                ReviewImage image = ReviewImage.builder()
                        .originalName(file.getOriginalFilename())
                        .storedName(storedName)
                        .imageUrl(IMAGE_URL + storedName)
                        .build();
                review.addImage(image); // 양방향 관계 설정
            }
        }

        //텍스트 수정
        review.setTitle(dto.getTitle());
        review.setContent(dto.getContent());
        review.setRating(dto.getRating());

        return toDto(review);
    }


    public void deleteReview(Long reviewId, String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        if (!review.getMember().getLoginId().equals(userId)) {
            throw new SecurityException("본인 리뷰만 삭제할 수 있습니다.");
        }

        for (ReviewImage image : review.getImages()) {
            fileService.delete(image.getStoredName());
        }

        reviewRepository.delete(review);
    }

    private ReviewDetailDto toDto(Review review) {
        return ReviewDetailDto.builder()
                .targetType(review.getTargetType())
                .targetId(review.getTargetId())
                .reviewId(review.getId())
                .title(review.getTitle())
                .content(review.getContent())
                .rating(review.getRating())
                .writer(review.getMember().getUsername())
                .createdAt(review.getCreatedAt())
                .imageUrls(review.getImages().stream()
                        .map(ReviewImage::getImageUrl)
                        .collect(Collectors.toList()))
                .build();
    }


    public Page<ReviewDetailDto> getReviewsByTarget(String targetType, Long targetId, Pageable pageable) {
        Page<Review> page = reviewRepository.findByTargetTypeAndTargetId(targetType, targetId, pageable);
        return page.map(this::toDto);
    }

    public Page<ReviewDetailDto> getReviewsByUser(String userId, Pageable pageable) {
        Member member = memberRepository.findByLoginId(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        Page<Review> page = reviewRepository.findByMember(member, pageable);
        return page.map(this::toDto);
    }


    public List<ReviewTargetDto> getReviewTargetsByPlan(Long planId, String userId) {
        TripPlan plan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획이 존재하지 않습니다."));

        if (!plan.getMember().getLoginId().equals(userId)) {
            throw new SecurityException("리뷰 접근 권한이 없습니다.");
        }

        List<ReviewTargetDto> results = new ArrayList<>();

        // ✅ 코스 전체 리뷰 타겟 추가
        if (plan.getCourse() != null) {
            Course course = plan.getCourse();
            results.add(ReviewTargetDto.of(
                    "course",  // 대상 타입
                    course.getCourseId(),
                    plan.getTitle() + " - 전체 여행 코스"
            ));

            // ✅ 코스 아이템(장소) 리뷰 타겟 추가
            for (CourseItem item : course.getItems()) {
                String categoryCode = item.getCategory().getCatCode();  // "tsp", "acc" 등
                String title = item.getCategory().getCatName() + " - ID " + item.getPlaceId();

                results.add(ReviewTargetDto.of(
                        categoryCode,
                        item.getPlaceId(),
                        title
                ));
            }
        }

        return results;
    }


    public ReviewTargetDto getCourseReviewTarget(Long planId) {
        TripPlan plan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("해당 계획을 찾을 수 없습니다."));

        Course course = plan.getCourse();
        if (course == null) {
            throw new IllegalStateException("계획에 연결된 코스가 없습니다.");
        }

        return ReviewTargetDto.of("course", course.getCourseId(), plan.getTitle() + " - 전체 여행 코스");
    }


    public ReviewDetailDto getReviewDetail(Long reviewId, String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));
        // 1. 본인 리뷰인지 확인 (오류 대신 boolean 값으로)
        boolean isOwner = false;
        if (userId != null) {
            isOwner = review.getMember().getLoginId().equals(userId);
        }
        // 2. toDto(review) 대신 DTO를 직접 빌드하여 isOwner 값 주입
        return ReviewDetailDto.builder()
                .reviewId(review.getId())
                .title(review.getTitle())
                .content(review.getContent())
                .rating(review.getRating())
                .writer(review.getMember().getUsername())
                .createdAt(review.getCreatedAt())
                .targetType(review.getTargetType())
                .targetId(review.getTargetId())
                .imageUrls(review.getImages().stream()
                        .map(ReviewImage::getImageUrl)
                        .collect(Collectors.toList()))
                .isOwner(isOwner) // :왼쪽을_가리키는_손_모양: isOwner 값을 DTO에 담아서 반환
                .build();
    }


    @Transactional
    public void createComplexReview(ComplexReviewReqDto dto, List<MultipartFile> images, String userId) {
        Member member = memberRepository.findByLoginId(userId).orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다.")); // --- 1. 코스 전체 리뷰 저장 ---
        Review mainReview = Review.builder()
                .title(dto.getMainReview()
                        .getTitle())
                .content(dto.getMainReview()
                        .getContent())
                .rating(dto.getMainReview().getRating())
                .targetType(dto.getMainReview().getTargetType())
                .targetId(dto.getMainReview()
                        .getTargetId())
                .member(member)
                .build(); // 메인 이미지 저장
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                String storedName = fileService.store(file);
                ReviewImage image = ReviewImage.builder().originalName(file.getOriginalFilename()).storedName(storedName).imageUrl(IMAGE_URL + storedName) // :왼쪽을_가리키는_손_모양: 4-1. 수정된 경로 사용
                        .build();

                mainReview.addImage(image);
            }
        }
        reviewRepository.save(mainReview); // 코스 리뷰 1개 저장// --- 2. 개별 관광지 리뷰들 저장 ---
        if (dto.getSpotReviews() != null) {
            for (SpotReviewDto spotDto : dto.getSpotReviews()) { // 별점을 선택했거나, 한 줄 평을 썼을 때만 저장
                if (spotDto.getRating() > 0 || (spotDto.getContent() != null && !spotDto.getContent().isBlank())) {
                    Review spotReview = Review.builder().title(mainReview.getTitle() + " - " + spotDto.getTargetType()) // 관광지 리뷰는 제목을 메인에서 따옴
                            .content(spotDto.getContent())
                            .rating(spotDto.getRating())
                            .targetType(spotDto.getTargetType())
                            .targetId(spotDto.getTargetId())
                            .member(member)
                            .build(); // 관광지 리뷰는 이미지 없음

                    reviewRepository.save(spotReview); // 관광지 리뷰 N개 저장
                }
            }
        }
    }


    }

