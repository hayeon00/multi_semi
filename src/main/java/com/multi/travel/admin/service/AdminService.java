package com.multi.travel.admin.service;

import com.multi.travel.admin.dto.TourSpotReqDto;
import com.multi.travel.admin.dto.TourSpotResDto;
import com.multi.travel.admin.dto.TourSpotSimpleResDto;
import com.multi.travel.common.exception.TourSpotNotFoundException;
import com.multi.travel.common.util.FileUploadUtils;
import com.multi.travel.tourspot.dto.TourSpotDTO;
import com.multi.travel.tourspot.entity.TourSpot;
import com.multi.travel.tourspot.repository.TspRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : chang
 * @filename : adminService
 * @since : 2025-11-10 월요일
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    @Value("${image.tourspot.image-dir}")
    private String IMAGE_DIR;

    @Value("${image.tourspot.image-url}")
    private String IMAGE_URL;

    private final TspRepository tspRepository;

    @Transactional
    public void insertTourSpot(TourSpotReqDto dto) {
        MultipartFile imageFile = dto.getFirstImageFile();
        String savedFileName = null;

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String extension = imageFile.getOriginalFilename()
                        .substring(imageFile.getOriginalFilename().lastIndexOf("."));
                String uniqueFileName = dto.getTitle().replaceAll("\\s+", "_")
                        + "_" + UUID.randomUUID().toString().replace("-", "") + extension;
                savedFileName = FileUploadUtils.saveFile(IMAGE_DIR, uniqueFileName, imageFile);
            }

            TourSpot spot = TourSpot.builder()
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .address(dto.getAddress())
                    .tel(dto.getTel())
                    .homepage(dto.getHomepage())
                    .mapx(dto.getMapx())
                    .mapy(dto.getMapy())
                    .areacode(dto.getAreacode())
                    .sigungucode(dto.getSigungucode())
                    .lDongRegnCd(dto.getLDongRegnCd())
                    .firstImage(savedFileName != null ? IMAGE_URL + savedFileName : null)
                    .status("Y")
                    .recCount(0)
                    .build();

            tspRepository.save(spot);

        } catch (IOException e) {
            if (savedFileName != null)
                FileUploadUtils.deleteFile(IMAGE_DIR, savedFileName);
            throw new RuntimeException("이미지 저장 실패", e);
        }

    }

    public void deleteSpot(Long id) {
        TourSpot tourSpot = tspRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관광지입니다."));

        tourSpot.setStatus("N");
        tspRepository.save(tourSpot);

    }

    /**
     * ✅ 관광지 상태 변경 (비활성화/복구)
     * @param id 관광지 ID
     * @param status 변경할 상태 ("Y" or "N")
     */
    @Transactional
    public void updateSpotStatus(Long id, String status) {
        TourSpot tourSpot = tspRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관광지입니다."));

        tourSpot.setStatus(status);  // 상태 변경
        tspRepository.save(tourSpot);

        log.info("[AdminService] 관광지 상태 변경 완료 → id: {}, status: {}", id, status);
    }

    /**
     * 관광지 수정 (이미지 교체 포함)
     */
    @Transactional
    public void updateSpot(Long id, TourSpotReqDto dto, MultipartFile file) {

        TourSpot tourSpot = tspRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관광지입니다."));

        String savedFileName = null;
        try {
            // 기존 이미지 파일명 (URL or 파일명)
            String oldImage = tourSpot.getFirstImage();

            // ✅ 기본 필드 업데이트
            tourSpot.setTitle(dto.getTitle());
            tourSpot.setDescription(dto.getDescription());
            tourSpot.setAddress(dto.getAddress());
            tourSpot.setTel(dto.getTel());
            tourSpot.setHomepage(dto.getHomepage());
            tourSpot.setMapx(dto.getMapx());
            tourSpot.setMapy(dto.getMapy());
            tourSpot.setAreacode(dto.getAreacode());
            tourSpot.setSigungucode(dto.getSigungucode());
            tourSpot.setLDongRegnCd(dto.getLDongRegnCd());
            tourSpot.setStatus("Y");

            // ✅ 새 이미지 업로드가 있을 경우
            if (file != null && !file.isEmpty()) {

                // 확장자 추출
                String extension = file.getOriginalFilename()
                        .substring(file.getOriginalFilename().lastIndexOf("."));

                // 관광지명 기반 유니크 파일명 생성
                String uniqueFileName = dto.getTitle().replaceAll("\\s+", "_")
                        + "_" + UUID.randomUUID().toString().replace("-", "")
                        + extension;

                // ✅ 새 파일 저장
                savedFileName = FileUploadUtils.saveFile(IMAGE_DIR, uniqueFileName, file);

                // ✅ 기존 이미지 삭제 (URL → 파일명 변환)
                if (!oldImage.startsWith("http://tong") && oldImage != null && !oldImage.isEmpty()) {
                    String oldFileName = oldImage.replace(IMAGE_URL, "");
                    FileUploadUtils.deleteFile(IMAGE_DIR, oldFileName);
                    log.info("[updateSpot] 기존 이미지 삭제: {}", oldFileName);
                }

                // ✅ DB에는 접근 가능한 URL 형태로 저장
                tourSpot.setFirstImage(IMAGE_URL + savedFileName);
                log.info("[updateSpot] 새 이미지 저장: {}", savedFileName);

            } else {
                // ✅ 이미지 변경 없음 — 기존 URL 그대로 유지
                tourSpot.setFirstImage(oldImage);
            }

            // ✅ 변경 감지로 UPDATE 반영
            tspRepository.save(tourSpot);

        } catch (IOException e) {
            // 실패 시 임시로 저장된 이미지 삭제
            if (savedFileName != null) {
                FileUploadUtils.deleteFile(IMAGE_DIR, savedFileName);
            }
            log.error("[updateSpot] 이미지 저장 중 오류 발생", e);
            throw new RuntimeException("관광지 이미지 저장 실패", e);
        }

        log.info("[updateSpot] 관광지 수정 완료: {} / {}", tourSpot.getId(), tourSpot.getFirstImage());
    }






    /** ✅ 전체 관광지 조회 (status 관계없이 모두) */
    public Page<TourSpotDTO> getAllTourSpotList(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        Page<TourSpot> tourPage = tspRepository.findAll(pageable);

        // ✅ Page<TourSpot> → Page<TourSpotDTO> 변환
        return tourPage.map(tsp -> TourSpotEntityToDTO(tsp, 0.0));
    }


    /** ✅ 관광지 상세 조회 (status 관계없이 조회 가능) */
    public TourSpotDTO getTourSpotDetail(Long id) {
        return TourSpotEntityToDTO(
                tspRepository.findById(id)
                        .orElseThrow(() -> new TourSpotNotFoundException(id)),
                0.0
        );
    }
    /** ✅ Entity → DTO 변환 (재사용) */
    private static List<TourSpotDTO> TourSpotListEntityToDto(Page<TourSpot> tsps) {
        return tsps.stream()
                .map(tsp -> TourSpotEntityToDTO(tsp, 0.0))
                .collect(Collectors.toList());
    }

    private static TourSpotDTO TourSpotEntityToDTO(TourSpot spot, Double distance) {
        return TourSpotDTO.builder()
                .id(spot.getId())
                .title(spot.getTitle())
                .address(spot.getAddress())
                .mapx(spot.getMapx())
                .mapy(spot.getMapy())
                .tel(spot.getTel())
                .firstImage(spot.getFirstImage())
                .firstImage2(spot.getFirstImage2())
                .areacode(spot.getAreacode())
                .recCount(Optional.ofNullable(spot.getRecCount()).orElse(0))
                .sigungucode(spot.getSigungucode())
                .lDongRegnCd(spot.getLDongRegnCd())
                .contentId(spot.getContentId())
                .status(spot.getStatus())
                //.cat_code("tsp")
                .createdAt(spot.getCreatedAt())
                .modifiedAt(spot.getModifiedAt())
                .build();
    }

    public List<TourSpotSimpleResDto> searchTourSpotByTitle(String title) {
        Pageable pageable = PageRequest.of(0, 50);
        Page<TourSpot> spotPage = tspRepository.findByTitleContainingIgnoreCase(title, pageable);
        List<TourSpot> spots = spotPage.getContent();

        return spots.stream()
                .map(spot -> TourSpotSimpleResDto.builder()
                        .id(spot.getId())
                        .title(spot.getTitle())
                        .address(spot.getAddress())
                        .tel(spot.getTel())
                        .status(spot.getStatus())

                        // ✅ 핵심: 이미지 절대 경로로 변환
                        .firstImage(
                                spot.getFirstImage() != null && !spot.getFirstImage().isBlank()
                                        ? (spot.getFirstImage().startsWith("http")
                                        ? spot.getFirstImage()
                                        : IMAGE_URL + spot.getFirstImage())
                                        : IMAGE_URL + "no-image.png"
                        )
                        .recCount(spot.getRecCount() != null ? spot.getRecCount() : 0)
                        .build()
                )
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public TourSpotResDto getSpotDetail(Long id) {
        TourSpot tourSpot = tspRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관광지입니다."));

        return TourSpotResDto.builder()
                .id(tourSpot.getId())
                .title(tourSpot.getTitle())
                .description(tourSpot.getDescription())
                .address(tourSpot.getAddress())
                .tel(tourSpot.getTel())
                .homepage(tourSpot.getHomepage())
                .mapx(tourSpot.getMapx())
                .mapy(tourSpot.getMapy())
                .areacode(tourSpot.getAreacode())
                .sigungucode(tourSpot.getSigungucode())
                .lDongRegnCd(tourSpot.getLDongRegnCd())
                .recCount(tourSpot.getRecCount())
                .status(tourSpot.getStatus())
                .firstImage(tourSpot.getFirstImage())
                .build();
    }
}
