package com.multi.travel.review.service;

import com.multi.travel.common.file.FileService;
import com.multi.travel.course.entity.Course;
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
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private final MemberRepository memberRepository;
    private final FileService fileService;
    private final ReviewImageRepository reviewImageRepository;
    private final TripPlanRepository tripPlanRepository;
    private final CourseRepository courseRepository;
    private final CourseItemRepository courseItemRepository;


    /* ============================
       리뷰 등록
       ============================ */
    public ReviewDetailDto createReview(ReviewReqDto dto, List<MultipartFile> images, String userId) {

        Long planId=dto.getTripPlanId();
        Member member = memberRepository.findByLoginId(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        TripPlan tripPlan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("여행 계획을 찾을 수 없습니다."));

        Review review = Review.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .rating(dto.getRating())
                .targetType(dto.getTargetType())
                .targetId(dto.getTargetId())
                .member(member)
                .tripPlan(tripPlan)
                .build();

        // 이미지 저장
        if (images != null && !images.isEmpty()) {
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



    /* ============================
       리뷰 삭제
       ============================ */
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


    /* ============================
       ★ 통합 DTO 변환 함수 (이미지 포함)
       ============================ */
    private ReviewDetailDto toDto(Review review) {

        List<String> imageUrls = review.getImages()
                .stream()
                .map(ReviewImage::getImageUrl)
                .toList();

        return ReviewDetailDto.builder()
                .reviewId(review.getId())
                .title(review.getTitle())
                .content(review.getContent())
                .rating(review.getRating())
                .writer(review.getMember().getUsername())
                .createdAt(review.getCreatedAt())
                .targetType(review.getTargetType())
                .targetId(review.getTargetId())
                .imageUrls(imageUrls)
                .build();
    }


    /* ============================
       Target 기준 리뷰 조회 (코스/관광지)
       ============================ */
    public Page<ReviewDetailDto> getReviewsByTarget(String targetType, Long targetId, Pageable pageable) {
        Page<Review> page = reviewRepository.findByTargetTypeAndTargetId(targetType, targetId, pageable);
        return page.map(this::toDto);
    }


    public Page<ReviewDetailDto> getReviewsByUser(String userId, Pageable pageable) {
        Member member = memberRepository.findByLoginId(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        Page<Review> reviewsPage = reviewRepository.findByMember(member, pageable);

        return reviewsPage.map(review -> {
            ReviewDetailDto dto = new ReviewDetailDto(review);
            List<String> imageUrls = review.getImages().stream().map(ReviewImage::getImageUrl).collect(Collectors.toList());
            dto.setImageUrls(imageUrls);
            return dto;
        });
    }


    /* ============================
       코스 리뷰 대상 반환
       ============================ */
    public ReviewTargetDto getCourseReviewTarget(Long planId) {

        TripPlan plan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("해당 계획을 찾을 수 없습니다."));

        Course course = plan.getCourse();
        if (course == null) {
            throw new IllegalStateException("계획에 연결된 코스가 없습니다.");
        }

        return ReviewTargetDto.of("course", course.getCourseId(), plan.getTitle() + " - 전체 여행 코스");
    }


    /* ============================
       복합 리뷰 저장 (코스 + 관광지)
       ============================ */
    @Transactional
    public void createComplexReview(ComplexReviewReqDto dto, List<MultipartFile> images, String userId) {

        Long planId = dto.getMainReview().getPlanId(); // MainReviewDto에서 planId를 가져옵니다.
        TripPlan tripPlan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("여행 계획을 찾을 수 없습니다. planId: " + planId));

        Member member = memberRepository.findByLoginId(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        // 1) 코스 전체 리뷰 저장
        Review mainReview = Review.builder()
                .title(dto.getMainReview().getTitle())
                .content(dto.getMainReview().getContent())
                .rating(dto.getMainReview().getRating())
                .targetType(dto.getMainReview().getTargetType())
                .targetId(dto.getMainReview().getTargetId())
                .member(member)
                .tripPlan(tripPlan)
                .build();

        // 메인 이미지 저장
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                String storedName = fileService.store(file);
                ReviewImage image = ReviewImage.builder()
                        .storedName(storedName)
                        .originalName(file.getOriginalFilename())
                        .imageUrl(IMAGE_URL + storedName)
                        .build();

                mainReview.addImage(image);
            }
        }

        reviewRepository.save(mainReview);

        // 2) 관광지 리뷰들 저장
        if (dto.getSpotReviews() != null) {
            for (SpotReviewDto spotDto : dto.getSpotReviews()) {

                boolean hasContent = spotDto.getContent() != null && !spotDto.getContent().isBlank();
                boolean hasRating = spotDto.getRating() > 0;

                if (hasContent || hasRating) {

                    Review spotReview = Review.builder()
                            .title(mainReview.getTitle() + " - " + spotDto.getTargetType())
                            .content(spotDto.getContent())
                            .rating(spotDto.getRating())
                            .targetType(spotDto.getTargetType())
                            .targetId(spotDto.getTargetId())
                            .member(member)
                            .tripPlan(tripPlan)
                            .build();

                    reviewRepository.save(spotReview);
                }
            }
        }
    }


    // ==========================================================
    // ⭐ 복합 리뷰 수정 로직 (수정 완료)
    // ==========================================================
    @Transactional
    public void updateComplexReview(Long mainReviewId, ComplexReviewReqDto dto, List<MultipartFile> newImages, String userId) {

        // 1. 메인 리뷰 엔티티 조회 및 권한 확인
        Review mainReview = reviewRepository.findById(mainReviewId)
                .orElseThrow(() -> new EntityNotFoundException("메인 리뷰를 찾을 수 없습니다. (ID: " + mainReviewId + ")"));

        Member member = memberRepository.findByLoginId(userId)
                .orElseThrow(() -> new AccessDeniedException("존재하지 않는 사용자입니다."));

        // 메인 리뷰 작성자 권한 확인 (필수)
        if (!mainReview.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("리뷰 수정 권한이 없습니다.");
        }

        // 2. 메인 리뷰 내용 업데이트 (Dirty Checking으로 자동 반영)
        mainReview.update(dto.getMainReview());

        // 3. 이미지 삭제 처리 (⭐ 수정된 로직 반영)
        if (dto.getDeletedImageUrls() != null && !dto.getDeletedImageUrls().isEmpty()) {
            for (String fullImageUrl : dto.getDeletedImageUrls()) {
                reviewImageRepository.findByImageUrl(fullImageUrl)
                        .ifPresent(image -> {
                            // 1. 서버 파일 삭제 (storedName 사용)
                            fileService.delete(image.getStoredName());
                            // 2. DB 엔티티 삭제 (mainReview의 images 리스트에서 제거. orphanRemoval=true에 의해 DB에서 삭제됨)
                            mainReview.getImages().remove(image);
                            log.debug("🗑️ 이미지 삭제 완료: {}", fullImageUrl);
                        });
            }
        }

        // 4. 새 이미지 업로드 및 연결 (⭐ 수정된 로직 반영)
        // 실제로 내용이 있는 파일만 필터링
        List<MultipartFile> validNewImages = newImages == null ? List.of() :
                newImages.stream()
                        .filter(f -> f != null && !f.isEmpty())
                        .toList();

        if (!validNewImages.isEmpty()) {
            for (MultipartFile file : validNewImages) {
                // 1. 파일 저장 및 storedName 획득
                String storedName = fileService.store(file);
                log.debug("📸 새 이미지 로컬 저장 완료: {}", storedName);

                // 2. ReviewImage 엔티티 생성
                ReviewImage image = ReviewImage.builder()
                        .originalName(file.getOriginalFilename())
                        .storedName(storedName)
                        .imageUrl(IMAGE_URL + storedName) // IMAGE_URL 변수 사용
                        .build();

                // 3. 메인 리뷰에 이미지 연결
                mainReview.addImage(image);
            }
        }

        // 5. 스팟 리뷰 생성, 업데이트 및 삭제 처리 (⭐ 수정된 로직 반영)
        if (dto.getSpotReviews() != null) {
            for (SpotReviewDto spotDto : dto.getSpotReviews()) {

                // ⭐ 신규 리뷰 등록 처리
                if (spotDto.getReviewId() == null) {
                    if (spotDto.getContent() != null && !spotDto.getContent().trim().isEmpty()) {
                        Review newSpotReview = Review.builder()
                                .member(member)
                                .tripPlan(mainReview.getTripPlan())
                                .targetType(spotDto.getTargetType())
                                .targetId(spotDto.getTargetId())
                                .rating(spotDto.getRating())
                                .content(spotDto.getContent())
                                .title(spotDto.getPlaceTitle()) // 스팟 리뷰 제목 설정
                                .build();
                        reviewRepository.save(newSpotReview);
                    }
                    continue; // 신규 등록 후 다음 DTO로 이동
                }

                // 기존 스팟 리뷰 수정/삭제 로직
                Review spotReview = reviewRepository.findById(spotDto.getReviewId())
                        .orElseThrow(() -> new EntityNotFoundException("스팟 리뷰를 찾을 수 없습니다: " + spotDto.getReviewId()));

                // ⭐ 삭제 판단: 내용과 별점 모두 없으면 삭제 (클라이언트의 삭제 의도)
                boolean isContentEmpty = spotDto.getContent() == null || spotDto.getContent().trim().isEmpty();
                boolean isRatingZeroOrNegative = spotDto.getRating() <= 0;

                if (isContentEmpty && isRatingZeroOrNegative) {
                    reviewRepository.delete(spotReview); // 삭제
                } else {
                    // 내용 또는 별점 중 하나라도 있으면 업데이트 (Dirty Checking으로 자동 반영)
                    spotReview.update(spotDto);
                }
            }
        }
    }
    // @Transactional 덕분에 mainReview와 연관된 엔티티는 자동 저장/업데이트/삭제됩니다.




    public List<ReviewDetailDto> getAllReviewsByPlanForEdit(Long planId, String userId) {

        Member member = memberRepository.findByLoginId(userId)
                .orElseThrow(() -> new AccessDeniedException("존재하지 않는 사용자입니다."));

        Long memberId = member.getId();
        List<Review> allReviews = reviewRepository.findAllByTripPlan_IdAndMember_Id(planId, memberId);

        if (allReviews.isEmpty()) {
            throw new AccessDeniedException("수정할 리뷰가 없거나 권한이 없습니다.");
        }


        // 4. Review 엔티티 목록을 ReviewDetailDto 목록으로 변환
        List<ReviewDetailDto> result = allReviews.stream()
                .map(review -> {
                    ReviewDetailDto dto = new ReviewDetailDto(review);

                    if (!"course".equalsIgnoreCase(review.getTargetType())) {

                        // 🚩 중요: Review 엔티티에 관광지 이름이 저장되어 있지 않은 경우,
                        // TripPlan의 CourseItem 등을 통해 장소명을 조회해야 합니다.

                        // 1. Review 엔티티의 title 필드에 장소명이 저장되어 있는지 확인
                        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {

                            // 2. 장소명이 누락된 경우, CourseItem 또는 장소 API를 통해 이름을 조회하고
                            //    dto.setTitle(...)을 호출하여 설정해야 합니다.
                            //    (예시: dto.setTitle(courseItemRepository.findPlaceName(review.getTargetId()));)

                            // 현재는 디버깅을 위해 임시 제목을 설정합니다.
                            dto.setTitle("관광지 리뷰 ID: " + review.getTargetId());
                        }
                        // ReviewDetailDto는 placeTitle 필드가 없으므로, title 필드에 장소명을 사용합니다.
                    }

                    // 이미지 URL 목록 설정
                    List<String> imageUrls = review.getImages().stream()
                            .map(ReviewImage::getImageUrl)
                            .collect(Collectors.toList());
                    dto.setImageUrls(imageUrls);

                    return dto;
                })
                .collect(Collectors.toList());

        return result;
    }


    public boolean hasReviewForPlan(Long planId, String userId) {
        Member member = memberRepository.findByLoginId(userId)
                .orElseThrow(() -> new AccessDeniedException("존재하지 않는 사용자입니다."));

        Long memberId = member.getId();

        List<Review> reviews = reviewRepository.findAllByTripPlan_IdAndMember_Id(planId, memberId);

        return !reviews.isEmpty();
    }
}