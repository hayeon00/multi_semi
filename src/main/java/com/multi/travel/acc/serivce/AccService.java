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
import com.multi.travel.common.util.FileUploadUtils;
import com.multi.travel.common.util.RoleUtils;
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
    private final ApiService apiService;

    @Value("${image.acc.image-dir}")
    private String IMAGE_DIR;

    @Value("${image.acc.image-url}")
    private String IMAGE_URL;

    private static final String DEFAULT_IMAGE = "default_acc.jpg";

    @PostConstruct
    public void checkImagePath() {
        log.info("✅ IMAGE_DIR: {}", IMAGE_DIR);
        log.info("✅ IMAGE_URL: {}", IMAGE_URL);
    }


    public List<ResAccDTO> getAccList(int page, int size, String sort, CustomUser customUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        if (RoleUtils.hasRole(customUser, RoleUtils.ADMIN)) {
            return convertToResAccDTO(accRepository.findAll(pageable).getContent());
        }
        return convertToResAccDTO(accRepository.findByStatus("Y", pageable).getContent());
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
        apiService.insertDetail(acc.getContentId(), acc.getCategory().getCatCode());
        Acc updatedAcc = accRepository.findById(id)
                .orElseThrow(() -> new AccommodationNotFound(id));
        return AccEntityToDTO(updatedAcc);
    }

    public List<ResDistanceAccDTO> getAccSortByDistance(int page, int size, @Valid long id) {
        Acc criteria = accRepository.findByIdAndStatus(id, "Y")
                .orElseThrow(() -> new AccommodationNotFound(id));

        Pageable pageable = PageRequest.of(page, size);
        List<AccHasDistanceProjection> lists = accRepository.findNearestWithDistanceRefactor(criteria.getMapx(), criteria.getMapy(), id, pageable);

        return convertToResDistanceAccDTO(lists);
    }

    @Transactional
    public AccDTO registAcc(AccDTO accDTO) { //관리자 전용
        Category category = categoryRepository.findById(accDTO.getCatCode()).orElseThrow(() -> new CategoryNotFoundException(accDTO.getCatCode()));
        Acc newAcc = Acc.builder()
                .address(accDTO.getAddress())
                .title(accDTO.getTitle())
                .tel(accDTO.getTel())
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
        accRepository.save(newAcc);

        MultipartFile imageFile = accDTO.getImageFile();
        String savedFileName = null;
        try {
            // 새 이미지 업로드 처리
            if (imageFile != null && !imageFile.isEmpty()) {

                // 확장자 추출 (.png / .jpg 등)
                String extension = imageFile.getOriginalFilename()
                        .substring(imageFile.getOriginalFilename().lastIndexOf("."));

                // 🔹 loginId 기반 유니크 파일명 생성 (공용 폴더에 저장)
                String uniqueFileName = newAcc.getId() + "_" + UUID.randomUUID().toString().replace("-", "") + extension;

                savedFileName = FileUploadUtils.saveFile(IMAGE_DIR, uniqueFileName, imageFile);

                // 🔹 새 파일명 DB 반영
                newAcc.updateImage(savedFileName);
            } else {
                // 기본 이미지 파일명 지정 (예: default_acc.jpg)
                newAcc.updateImage(DEFAULT_IMAGE);
            }
        } catch (IOException e) {
            if (savedFileName != null) {
                FileUploadUtils.deleteFile(IMAGE_DIR, savedFileName);
            }
            throw new RuntimeException("숙소 이미지 저장 실패", e);
        }
        return AccEntityToDTO(newAcc);
    }

    @Transactional
    public AccDTO updateAcc(AccDTO accDTO) { //관리자 전용
        Acc acc = accRepository.findById(accDTO.getId()).orElseThrow(() -> new AccommodationNotFound(accDTO.getId()));

        MultipartFile imageFile = accDTO.getImageFile();
        String savedFileName = null;
        try {
            String oldImage = acc.getFirstImage();

            // 새 이미지 업로드 처리
            if (imageFile != null && !imageFile.isEmpty()) {

                // 확장자 추출 (.png / .jpg 등)
                String extension = imageFile.getOriginalFilename()
                        .substring(imageFile.getOriginalFilename().lastIndexOf("."));

                // 🔹 loginId 기반 유니크 파일명 생성 (공용 폴더에 저장)
                String uniqueFileName = acc.getId() + "_" + UUID.randomUUID().toString().replace("-", "") + extension;

                savedFileName = FileUploadUtils.saveFile(IMAGE_DIR, uniqueFileName, imageFile);
                String imageUrl = IMAGE_URL + savedFileName;
                if (oldImage != null && !oldImage.isEmpty() && !oldImage.equals(DEFAULT_IMAGE)) {
                    FileUploadUtils.deleteFile(IMAGE_DIR, oldImage);
                    log.info("[Acc] 기존 이미지 삭제: {}", oldImage);
                }
                // 🔹 새 파일명 DB 반영
                acc.updateImage(imageUrl);
            }
        } catch (IOException e) {
            if (savedFileName != null) FileUploadUtils.deleteFile(IMAGE_DIR, savedFileName);
            throw new RuntimeException("숙소 이미지 저장 실패", e);
        } catch (Exception e) {
            throw new RuntimeException("숙소 업데이트 중 예기치 못한 오류 발생", e);
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
                        .distanceMeter(list.getDistanceKm()*1000)
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
