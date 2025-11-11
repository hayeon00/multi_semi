package com.multi.travel.common.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : FileService
 * @since : 2025. 11. 12. 수요일
 */

@Service
public class FileService {

    @Value("${image.image-dir}")
    private String imageDir;

    @Value("${image.image-url}")
    private String imageUrl;

    public String store(MultipartFile file) {
        try {
            // 저장할 파일 이름 생성
            String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // 저장할 위치
            File dest = new File(imageDir + storedName);
            file.transferTo(dest);

            // 접근 가능한 URL 반환
            return storedName;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + e.getMessage());
        }
    }

    public void delete(String storedName) {
        File file = new File(imageDir + storedName);
        if (file.exists()) {
            file.delete();
        }
    }

    public String getImageUrl(String storedName) {
        return imageUrl + storedName;
    }
}


