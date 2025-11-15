package com.multi.travel.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Please explain the class!!!
 *
 * @author : chang
 * @filename : WebConfogProfile
 * @since : 2025-11-10 월요일
 */
@Configuration
public class WebConfigProfile implements WebMvcConfigurer {

    @Value("${image.member.image-dir}")
    private String MEMBER_DIR;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:" + MEMBER_DIR);
    }
}
