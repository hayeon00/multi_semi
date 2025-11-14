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
import com.multi.travel.review.dto.ReviewDetailDto;
import com.multi.travel.review.dto.ReviewReqDto;
import com.multi.travel.review.dto.ReviewTargetDto;
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

        // 본인 리뷰인지 확인 (선택사항)
        if (!review.getMember().getLoginId().equals(userId)) {
            throw new SecurityException("본인의 리뷰만 조회할 수 있습니다.");
        }

        return toDto(review); // 이미지를 포함하여 DTO로 변환
    }




}

