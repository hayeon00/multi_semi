package com.multi.travel.common.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileService {

    //@Value("${file.upload-dir:/tmp}") // 기본값 지정 (예: /tmp)
    @Value("${image.review.image-dir}") // 기본값 지정 (예: /tmp)
    private String uploadDir;


    /**
     * 파일 저장 후 저장된 파일명 반환
     */
    public String store(MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            String ext = getExtension(originalName);
            String safeName = UUID.randomUUID() + ext;

            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir); // 디렉토리가 없으면 생성

            Path dest = dir.resolve(safeName);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            return safeName;
        } catch (IOException e) {
            log.error("파일 저장 실패", e);
            throw new RuntimeException("파일 저장 실패: " + file.getOriginalFilename(), e);
        }
    }

    private String getExtension(String filename) {
        return filename != null && filename.contains(".")
                ? filename.substring(filename.lastIndexOf("."))
                : "";
    }

    /**
     * 저장된 파일 삭제
     */
    public void delete(String storedFileName) {
        try {
            Path path = Paths.get(uploadDir, storedFileName);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", storedFileName, e);
        }
    }


}
