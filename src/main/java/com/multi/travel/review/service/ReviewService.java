package com.multi.travel.review.service;

import com.multi.travel.common.file.FileService;
import com.multi.travel.member.entity.Member;
import com.multi.travel.member.repository.MemberRepository;
import com.multi.travel.review.dto.ReviewDetailDto;
import com.multi.travel.review.dto.ReviewReqDto;
import com.multi.travel.review.entity.Review;
import com.multi.travel.review.entity.ReviewImage;
import com.multi.travel.review.repository.ReviewImageRepository;
import com.multi.travel.review.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final MemberRepository memberRepository;

    private final FileService fileService;

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

        // 이미지 저장
        if (images != null && !images.isEmpty()) {
            List<ReviewImage> reviewImages = images.stream()
                    .map(file -> {
                        String storedName = fileService.store(file);
                        return ReviewImage.builder()
                                .originalName(file.getOriginalFilename())
                                .storedName(storedName)
                                .imageUrl("/uploads/" + storedName)
                                .review(review)
                                .build();
                    }).toList();
            review.setImages(reviewImages);
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

        // 기존 이미지 삭제
        for (ReviewImage img : review.getImages()) {
            fileService.delete(img.getStoredName());
        }
        review.getImages().clear();

        // 새 이미지 등록
        if (newImages != null && !newImages.isEmpty()) {
            List<ReviewImage> newReviewImages = newImages.stream()
                    .map(file -> {
                        String storedName = fileService.store(file);
                        return ReviewImage.builder()
                                .originalName(file.getOriginalFilename())
                                .storedName(storedName)
                                .imageUrl("/uploads/" + storedName)
                                .review(review)
                                .build();
                    }).toList();
            review.setImages(newReviewImages);
        }

        // 내용 수정
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

        // 이미지 삭제
        for (ReviewImage image : review.getImages()) {
            fileService.delete(image.getStoredName());
        }

        reviewRepository.delete(review);
    }

    private ReviewDetailDto toDto(Review review) {
        return ReviewDetailDto.builder()
                .reviewId(review.getId())
                .title(review.getTitle())
                .content(review.getContent())
                .rating(review.getRating())
                .writer(review.getMember().getUsername())
                .createdAt(review.getCreatedAt())
                .imageUrls(review.getImages().stream()
                        .map(ReviewImage::getImageUrl)
                        .toList())
                .build();
    }

    public List<ReviewDetailDto> getReviewsByUser(String userId) {
        Member member = memberRepository.findByLoginId(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        List<Review> reviews = reviewRepository.findByMember(member);
        return reviews.stream().map(this::toDto).toList();
    }

    public List<ReviewDetailDto> getReviewsByTarget(String targetType, Long targetId) {
        List<Review> reviews = reviewRepository.findByTargetTypeAndTargetId(targetType, targetId);
        return reviews.stream().map(this::toDto).toList();
    }

}
