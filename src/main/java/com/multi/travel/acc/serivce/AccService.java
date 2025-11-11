package com.multi.travel.acc.serivce;

/*
 * Please explain the class!!!
 *
 * @filename    : AccService
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 9. 일요일
 */

import com.multi.travel.acc.dto.AccDTO;
import com.multi.travel.acc.entity.Acc;
import com.multi.travel.acc.repository.AccRepository;
import com.multi.travel.category.CategoryRepository;
import com.multi.travel.category.entity.Category;
import com.multi.travel.common.exception.AccommodationNotFound;
import com.multi.travel.common.exception.CategoryNotFoundException;
import com.multi.travel.common.util.FileUploadUtils;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccService {
    private final AccRepository accRepository;
    private final CategoryRepository categoryRepository;

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


    private static List<AccDTO> TourSpotListEntityToDto(Page<Acc> accs) {
        return accs.stream()
                .map(acc -> AccEntityToDTO(acc, 0.0))
                .collect(Collectors.toList());
    }

    private static AccDTO AccEntityToDTO(Acc acc, Double distance) {
        return AccDTO.builder()
                .id(acc.getId())
                .title(acc.getTitle())
                .address(acc.getAddress())
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
                .distanceMeter(distance * 1000)
                .catCode("acc")
                .createdAt(acc.getCreatedAt())
                .modifiedAt(acc.getModifiedAt())
                .build();
    }

    public List<AccDTO> getAccListPaging(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        return TourSpotListEntityToDto(accRepository.findByStatus("Y", pageable));
    }

    public AccDTO getAccDetail(@Valid long id) {
        Acc entity = accRepository.findByIdAndStatus(id, "Y")
                .orElseThrow(() -> new AccommodationNotFound(id));
        return AccEntityToDTO(entity, 0.0);

    }

    public List<AccDTO> getAccSortByDistance(int page, int size, @Valid long id) {
        Acc criteria = accRepository.findByIdAndStatus(id, "Y")
                .orElseThrow(() -> new AccommodationNotFound(id));


        Pageable pageable = PageRequest.of(page, size);


        List<Object[]> results = accRepository.findNearestWithDistance(criteria.getMapx(), criteria.getMapy(), id, pageable);
        return results.stream()
                .map(obj -> {
                    Long accId = (Long) obj[0];
                    Double distance = (Double) obj[1];
                    Acc acc = accRepository.findById(accId).orElseThrow(() -> new AccommodationNotFound(accId));
                    acc.setDistanceKm(distance);
                    return AccEntityToDTO(acc, distance);
                })
                .collect(Collectors.toList());

    }

    @Transactional
    public AccDTO registAcc(AccDTO accDTO) {
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
        return AccEntityToDTO(newAcc, 0.0);
    }

    @Transactional
    public AccDTO updateAcc(AccDTO accDTO) {
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
        return AccEntityToDTO(acc, 0.0);
    }

    @Transactional
    public Object deleteAcc(@Valid Long accId) {
        Acc acc = accRepository.findById(accId).orElseThrow(() -> new AccommodationNotFound(accId));
        if (acc.getStatus().equals("Y")) {
            acc.changeStatus();
        }
        return AccEntityToDTO(acc, 0.0);
    }
}
