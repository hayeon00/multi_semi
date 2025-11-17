package com.multi.travel.acc.serivce;

/*
 * Please explain the class!!!
 *
 * @filename    : AccService
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 9. 일요일
 */

import com.multi.travel.acc.dto.*;
import com.multi.travel.acc.entity.Acc;
import com.multi.travel.acc.repository.AccRepository;
import com.multi.travel.api.service.ApiService;
import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.category.CategoryRepository;
import com.multi.travel.category.entity.Category;
import com.multi.travel.common.exception.AccommodationNotFound;
import com.multi.travel.common.exception.CategoryNotFoundException;
import com.multi.travel.common.exception.TourSpotNotFoundException;
import com.multi.travel.common.util.FileUploadUtils;
import com.multi.travel.common.util.RoleUtils;
import com.multi.travel.tourspot.entity.TourSpot;
import com.multi.travel.tourspot.repository.TspRepository;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccService {
    private final AccRepository accRepository;
    private final CategoryRepository categoryRepository;
    private final TspRepository tspRepository;
    private final ApiService apiService;

    @Value("${image.acc.image-dir}")
    private String IMAGE_DIR;

    @Value("${image.acc.image-url}")
    private String IMAGE_URL;

    @Value("${image.default.image-url}+no-image.png")
    private String DEFAULT_IMAGE;

    @PostConstruct
    public void checkImagePath() {
        log.info("✅ IMAGE_DIR: {}", IMAGE_DIR);
        log.info("✅ IMAGE_URL: {}", IMAGE_URL);
    }


    public Map<String, Object> getAccList(int page, int size, String sort, CustomUser customUser) {
        Page<Acc> accPage;
        Pageable pageable;
        if (sort.equals("recCount")) {
            pageable = PageRequest.of(page, size, Sort.by(sort).descending());
        } else {
            pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        }

        if (RoleUtils.hasRole(customUser, RoleUtils.ADMIN)) {
            accPage = accRepository.findAll(pageable);
        } else {
            accPage = accRepository.findByStatus("Y", pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalPages", accPage.getTotalPages());
        response.put("totalElements", accPage.getTotalElements());
        response.put("contents", convertToResAccDTO(accPage.getContent()));
        return response;
    }

    public Map<String, Object> getAccSearch(int page, int size, String sort, String keyword, CustomUser customUser) {
        Pageable pageable;

        if (sort.equals("recCount")) {
            pageable = PageRequest.of(page, size, Sort.by(sort).descending());
        } else {
            pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        }

        Page<Acc> accPage;
        if (RoleUtils.hasRole(customUser, RoleUtils.ADMIN)) {
            accPage = accRepository.search(keyword, pageable);
        } else {
            accPage = accRepository.statusAndSearch("Y", keyword, pageable);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("totalPages", accPage.getTotalPages());
        response.put("totalElements", accPage.getTotalElements());
        response.put("contents", convertToResAccDTO(accPage.getContent()));
        return response;
    }

    public AccDTO getAccDetail(@Valid long id, CustomUser customUser) {
        Acc acc;
        if (RoleUtils.hasRole(customUser, RoleUtils.ADMIN)) {
            acc = accRepository.findById(id)
                    .orElseThrow(() -> new AccommodationNotFound(id));
        } else {
            acc = accRepository.findByIdAndStatus(id, "Y")
                    .orElseThrow(() -> new AccommodationNotFound(id));
        }
        if (acc.getDescription() == null || acc.getHomepage() == null) {
            apiService.insertDetail(acc.getContentId(), acc.getCategory().getCatCode());
        }

        Acc updatedAcc = accRepository.findById(id)
                .orElseThrow(() -> new AccommodationNotFound(id));
        return AccEntityToDTO(updatedAcc);
    }

    public Map<String, Object> getAccSortByDistance(int page, int size, @Valid long id, CustomUser customUser) {
        Pageable pageable = PageRequest.of(page, size);
        TourSpot criteria;
        Page<AccHasDistanceProjection> accPage;
        if (RoleUtils.hasRole(customUser, RoleUtils.ADMIN)) {
            criteria = tspRepository.findById(id)
                    .orElseThrow(() -> new TourSpotNotFoundException(id));
            accPage = accRepository.findNearestWithDistanceAdmin(criteria.getMapx(), criteria.getMapy(), pageable);
        } else {
            criteria = tspRepository.findByIdAndStatus(id, "Y")
                    .orElseThrow(() -> new TourSpotNotFoundException(id));
            accPage = accRepository.findNearestWithDistanceAndStatus(criteria.getMapx(), criteria.getMapy(), pageable);
        }

        Map<String, Object> response = new HashMap<>();

        response.put("totalPages", accPage.getTotalPages());
        response.put("contents", convertToResDistanceAccDTO(accPage.getContent()));
        return response;
    }

    @Transactional
    public AccDTO registAcc(AccDTO accDTO) { //관리자 전용
        Category category = categoryRepository.findById(accDTO.getCatCode()).orElseThrow(() -> new CategoryNotFoundException(accDTO.getCatCode()));
        Acc newAcc = Acc.builder()
                .address(accDTO.getAddress())
                .title(accDTO.getTitle())
                .tel(accDTO.getTel())
                .description(accDTO.getDescription())
                .mapx(accDTO.getMapx())
                .mapy(accDTO.getMapy())
                .areacode(accDTO.getAreacode())
                .sigungucode(accDTO.getSigungucode())
                .lDongRegnCd(accDTO.getLDongRegnCd())
                .contentId(accDTO.getContentId())
                .category(category)
                .status("Y")
                .recCount(0)
                .build();
        accRepository.save(newAcc); // 🔹 ID 생성 위해 먼저 저장해야 함
        MultipartFile imageFile = accDTO.getImageFile();
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                // ⭐ 확장자 제외한 기본 파일명 생성
                String baseName = newAcc.getId() + "_" + UUID.randomUUID().toString().replace("-", "");
                // ⭐ 실제 저장 (saveFile이 확장자 자동 추가)
                String savedFileName = FileUploadUtils.saveFile(IMAGE_DIR, baseName, imageFile);
                // ⭐ DB엔 URL 형태로 저장
                String imageUrl = IMAGE_URL + savedFileName;
                newAcc.updateImage(imageUrl);
            }
        } catch (IOException e) {
            throw new RuntimeException("숙소 이미지 저장 실패", e);
        }
        return AccEntityToDTO(newAcc);
    }


    @Transactional
    public AccDTO updateAcc(AccDTO accDTO) {

        Acc acc = accRepository.findById(accDTO.getId())
                .orElseThrow(() -> new AccommodationNotFound(accDTO.getId()));

        MultipartFile imageFile = accDTO.getImageFile();

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String oldImageUrl = acc.getFirstImage();
                String baseName = acc.getId() + "_" + UUID.randomUUID().toString().replace("-", "");
                String savedFileName = FileUploadUtils.saveFile(IMAGE_DIR, baseName, imageFile);
                String imageUrl = IMAGE_URL + savedFileName;
                if (oldImageUrl != null && !oldImageUrl.isEmpty() && !oldImageUrl.equals(DEFAULT_IMAGE)) {
                    String oldFileName = oldImageUrl.replace(IMAGE_URL, ""); // URL → 파일명 변환
                    FileUploadUtils.deleteFile(IMAGE_DIR, oldFileName);
                }
                acc.updateImage(imageUrl);
            }

        } catch (IOException e) {
            throw new RuntimeException("숙소 이미지 저장 실패", e);
        }

        acc.updateInfo(accDTO);
        return AccEntityToDTO(acc);
    }


    @Transactional
    public AccDTO deleteAcc(@Valid Long accId) { //관리자 전용
        Acc acc = accRepository.findById(accId).orElseThrow(() -> new AccommodationNotFound(accId));
        if (acc.getStatus().equals("Y")) {
            acc.changeStatus();
        }
        return AccEntityToDTO(acc);
    }


    private static List<ResDistanceAccDTO> convertToResDistanceAccDTO(List<AccHasDistanceProjection> lists) {
        return lists.stream()
                .map(list -> ResDistanceAccDTO.builder()
                        .id(list.getId())
                        .title(list.getTitle())
                        .address(list.getAddress())
                        .recCount(list.getRecCount())
                        .firstImage(list.getFirstImage())
                        .distanceMeter(list.getDistanceKm()
                                .multiply(BigDecimal.valueOf(1000))
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue())
                        .build()
                ).toList();
    }

    private static List<ResAccDTO> convertToResAccDTO(List<Acc> lists) {
        return lists.stream()
                .map(list -> ResAccDTO.builder()
                        .id(list.getId())
                        .title(list.getTitle())
                        .address(list.getAddress())
                        .recCount(list.getRecCount())
                        .firstImage(list.getFirstImage())
                        .status(list.getStatus())
                        .mapx(list.getMapx())
                        .mapy(list.getMapy())
                        .build()
                ).toList();
    }

    private static AccDTO AccEntityToDTO(Acc acc) {
        return AccDTO.builder()
                .id(acc.getId())
                .title(acc.getTitle())
                .address(acc.getAddress())
                .description(acc.getDescription())
                .homepage(acc.getHomepage())
                .mapx(acc.getMapx())
                .mapy(acc.getMapy())
                .tel(acc.getTel())
                .firstImage(acc.getFirstImage())
                .firstImage2(acc.getFirstImage2())
                .areacode(acc.getAreacode())
                .recCount(acc.getRecCount() != null ? acc.getRecCount() : 0)
                .sigungucode(acc.getSigungucode())
                .lDongRegnCd(acc.getLDongRegnCd())
                .contentId(acc.getContentId())
                .status(acc.getStatus())
                .catCode("acc")
                .createdAt(acc.getCreatedAt())
                .modifiedAt(acc.getModifiedAt())
                .build();
    }

    public Map<String, Object> getAccSimpleList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Acc> accPage = accRepository.findByStatus("Y", pageable);

        List<ResAccSimpleDTO> list = accPage.getContent().stream()
                .map(a -> ResAccSimpleDTO.builder()
                        .id(a.getId())
                        .title(a.getTitle())
                        .mapx(a.getMapx())
                        .mapy(a.getMapy())
                        .build())
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", accPage.getTotalPages());
        result.put("contents", list);
        return result;
    }
}
