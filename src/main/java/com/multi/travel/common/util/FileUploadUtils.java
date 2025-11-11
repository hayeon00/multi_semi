package com.multi.travel.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : FileUploadUtils
 * @since : 2025. 11. 9. 일요일
 */

@Slf4j
public class FileUploadUtils {

    public static String saveFile(String uploadDir, String fileName, MultipartFile file) throws IOException, IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFileExtension = Optional.ofNullable(file.getOriginalFilename())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(file.getOriginalFilename().lastIndexOf(".")))
                .orElse("");

        String fullFileName = fileName + originalFileExtension;
        Path filePath = uploadPath.resolve(fullFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fullFileName;
    }



    public static boolean deleteFile(String uploadDir, String fileName) {

        boolean result = false;
        Path uploadPath = Paths.get(uploadDir);
        // 디렉토리가 없으면 삭제할 파일이 없으므로 true 반환
        if(!Files.exists(uploadPath)) {
            result = true;
        }
        try {
            Path filePath = uploadPath.resolve(fileName);
            Files.delete(filePath);  // 파일 삭제
            result = true;
        }catch (IOException ex){

            log.info("Could not delete file: " + fileName, ex);
        }

        return result;
    }
}
