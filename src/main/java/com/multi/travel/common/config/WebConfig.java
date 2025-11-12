package com.multi.travel.common.config;

/*
 * Please explain the class!!!
 *
 * @filename    : AppConfig
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 8. 토요일
 */

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${image.review.add-resource-locations}")
    private String reviewLocation;

    @Value("${image.review.add-resource-handler}")
    private String reviewHandler;

    // ✅ tourspot 이미지 경로
    @Value("${image.tourspot.add-resource-locations}")
    private String tourspotLocation;

    @Value("${image.tourspot.add-resource-handler}")
    private String tourspotHandler;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 기본 static 리소스 유지
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");

        // 이미지 업로드 경로 추가
        registry.addResourceHandler("/reviewimgs/**")
                .addResourceLocations("file:/Users/chang/Desktop/upload/reviewimgs/");

        registry.addResourceHandler("/tourspotimgs/**")  // ✅ 관광지 추가
                .addResourceLocations("file:/Users/chang/Desktop/upload/tourspotimgs/");
    }

}

