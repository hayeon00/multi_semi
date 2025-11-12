package com.multi.travel.common.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : FileService
 * @since : 2025. 11. 12. 수요일
 */

@Service
@Slf4j
public class FileService {

    @Value("${file.upload-dir:}") // 기본값도 지정 가능
    private String uploadDir;


    /**
     * 파일 저장 후 저장된 파일명 반환
     */
    public String store(MultipartFile file) {
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File target = new File(uploadDir, storedName);

        try {
            file.transferTo(target);
            return storedName;
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("파일 저장 실패: " + file.getOriginalFilename());
        }
    }

    public void delete(String storedFileName) {
        try {
            Path path = Paths.get(uploadDir, storedFileName);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", storedFileName, e);
        }
    }



}


