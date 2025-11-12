package com.multi.travel.admin.service;

import com.multi.travel.admin.controller.dto.TourSpotReqDto;
import com.multi.travel.admin.repository.TourSpotRepository;
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
    private final TourSpotRepository tourSpotRepository;

    private final TspRepository tspRepository;

    @Transactional
    public void insertTourSpot(TourSpotReqDto dto) {

        MultipartFile imageFile = dto.getFirstImageFile();
        String savedFileName = null;

        try {
            // ✅ 이미지 파일이 있을 경우
            if (imageFile != null && !imageFile.isEmpty()) {

                // 확장자 추출 (.png, .jpg 등)
                String extension = imageFile.getOriginalFilename()
                        .substring(imageFile.getOriginalFilename().lastIndexOf("."));

                // 관광지 제목 기반 + UUID 조합 파일명 생성
                String uniqueFileName = dto.getTitle().replaceAll("\\s+", "_")
                        + "_" + UUID.randomUUID().toString().replace("-", "") + extension;

                // ✅ 파일 저장 (FileUploadUtils 활용)
                savedFileName = FileUploadUtils.saveFile(IMAGE_DIR, uniqueFileName, imageFile);

                log.info("[insertTourSpot] 이미지 업로드 성공: {}", savedFileName);
            }

            // ✅ 관광지 엔티티 생성
            TourSpot tourSpot = TourSpot.builder()
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .address(dto.getAddress())
                    .tel(dto.getTel())
                    .mapx(dto.getMapx())
                    .mapy(dto.getMapy())
                    .areacode(dto.getAreacode())
                    .sigungucode(dto.getSigungucode())
                    .lDongRegnCd(dto.getLDongRegnCd())
                    .firstImage(savedFileName)     // ✅ 업로드된 이미지명 저장
                    .status("Y")
                    .recCount(0)
                    .build();

            tourSpotRepository.save(tourSpot);
            log.info("[insertTourSpot] 관광지 등록 완료: {}", tourSpot.getTitle());

        } catch (IOException e) {
            if (savedFileName != null) {
                FileUploadUtils.deleteFile(IMAGE_DIR, savedFileName);
            }
            throw new RuntimeException("관광지 이미지 저장 실패", e);
        }
    }

    public void deleteSpot(Long id) {
        TourSpot tourSpot = tourSpotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관광지입니다."));

        tourSpot.setStatus("N");
        tourSpotRepository.save(tourSpot);

    }

    @Transactional
    public void updateSpot(Long id, TourSpotReqDto dto) {

        // 1️⃣ 기존 관광지 조회
        TourSpot tourSpot = tourSpotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관광지입니다."));

        // 2️⃣ 변경할 필드 갱신
        tourSpot.setTitle(dto.getTitle());
        tourSpot.setDescription(dto.getDescription());
        tourSpot.setAddress(dto.getAddress());
        tourSpot.setTel(dto.getTel());
        tourSpot.setMapx(dto.getMapx());
        tourSpot.setMapy(dto.getMapy());
        tourSpot.setAreacode(dto.getAreacode());
        tourSpot.setSigungucode(dto.getSigungucode());
        tourSpot.setLDongRegnCd(dto.getLDongRegnCd());
        tourSpot.setStatus("Y"); // 수정 시 다시 활성화 상태로


        // 3️⃣ save 호출 → 변경 감지(Dirty Checking)로 update SQL 자동 반영
        tourSpotRepository.save(tourSpot);
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
//                .distanceMeter(distance * 1000)
                .catCode("tsp")
                .createdAt(spot.getCreatedAt())
                .modifiedAt(spot.getModifiedAt())
                .build();
    }


}
