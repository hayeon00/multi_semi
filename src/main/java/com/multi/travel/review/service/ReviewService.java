package com.multi.travel.review.service;


import com.multi.travel.common.util.FileUploadUtils;
import com.multi.travel.member.entity.Member;
import com.multi.travel.plan.entity.TripPlan;
import com.multi.travel.plan.repository.TripPlanRepository;
import com.multi.travel.review.dto.ReviewDetailDto;
import com.multi.travel.review.dto.ReviewReqDto;
import com.multi.travel.review.dto.ReviewResDto;
import com.multi.travel.review.entity.Review;
import com.multi.travel.review.entity.ReviewImage;
import com.multi.travel.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    @Value("${image.image-dir}")
    private String IMAGE_DIR;

    @Value("${image.image-url}")
    private String IMAGE_URL;

    private final ReviewRepository reviewRepository;
    private final TripPlanRepository tripPlanRepository;

    public ReviewResDto createReview(ReviewReqDto dto) {
        TripPlan plan = tripPlanRepository.findById(dto.getTripPlanId())
                .orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다."));

        Member member = plan.getMember();

        Review review = Review.builder()
                .tripPlan(plan)
                .member(member)
                .title(dto.getTitle())
                .content(dto.getContent())
                .rating(dto.getRating())
                .images(new ArrayList<>())
                .build();

        List<MultipartFile> images = dto.getReviewImages();
        List<String> imageUrls = new ArrayList<>();

        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    try {
                        String originalFilename = image.getOriginalFilename();
                        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                        String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + extension;

                        String savedFileName = FileUploadUtils.saveFile(IMAGE_DIR, uniqueFileName, image);
                        String imageUrl = IMAGE_URL + savedFileName;

                        ReviewImage reviewImage = ReviewImage.builder()
                                .imageUrl(imageUrl)
                                .review(review)
                                .build();

                        review.getImages().add(reviewImage);
                        imageUrls.add(imageUrl);

                        log.info("이미지 저장 위치: {}", IMAGE_DIR);
                        log.info("저장된 파일명: {}", savedFileName);
                        log.info("접근 가능한 URL: {}", imageUrl);

                    } catch (IOException e) {
                        log.error("이미지 저장 실패: {}", e.getMessage());
                        throw new RuntimeException("이미지 저장 실패", e);
                    }
                }
            }
        }

        log.info("👉 받은 이미지 수: {}", images == null ? "null" : images.size());

        reviewRepository.save(review);

        return ReviewResDto.builder()
                .message("리뷰가 성공적으로 등록되었습니다.")
                .imageUrls(imageUrls)
                .build();
    }


    public List<ReviewDetailDto> getReviewsByTripPlan(Long tripPlanId) {
        List<Review> reviews = reviewRepository.findByTripPlanId(tripPlanId);

        return reviews.stream().map(review -> ReviewDetailDto.builder()
                .reviewId(review.getId())
                .title(review.getTitle())
                .content(review.getContent())
                .rating(review.getRating())
                .writer(review.getMember().getMemberName()) // 또는 username, nickname 등
                .createdAt(review.getCreatedAt())
                .imageUrls(
                        review.getImages().stream()
                                .map(ReviewImage::getImageUrl)
                                .toList()
                )
                .build()
        ).toList();
    }


    public List<ReviewDetailDto> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(review -> ReviewDetailDto.builder()
                        .reviewId(review.getId())
                        .title(review.getTitle())
                        .content(review.getContent())
                        .rating(review.getRating())
                        .writer(review.getMember().getMemberName()) // member.getUsername() 등도 가능
                        .createdAt(review.getCreatedAt())
                        .imageUrls(
                                review.getImages().stream()
                                        .map(ReviewImage::getImageUrl)
                                        .toList()
                        )
                        .build())
                .toList();
    }


    public ReviewDetailDto getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        return ReviewDetailDto.builder()
                .reviewId(review.getId())
                .title(review.getTitle())
                .content(review.getContent())
                .rating(review.getRating())
                .writer(review.getMember().getMemberName())
                .createdAt(review.getCreatedAt())
                .imageUrls(
                        review.getImages().stream()
                                .map(ReviewImage::getImageUrl)
                                .toList()
                )
                .build();
    }


}

