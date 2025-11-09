package com.multi.travel.common.util;

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




}
