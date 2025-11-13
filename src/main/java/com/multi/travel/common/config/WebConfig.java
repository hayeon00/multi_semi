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

    @Value("${image.acc.add-resource-locations}")
    private String accLocations;

    @Value("${image.acc.add-resource-handler}")
    private String accHandler;

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
        registry.addResourceHandler(reviewHandler)
                .addResourceLocations(reviewLocation);
        registry.addResourceHandler(accHandler)
                .addResourceLocations(accLocations);
        registry.addResourceHandler(tourspotHandler)  // ✅ 관광지 추가
                .addResourceLocations(tourspotLocation);
    }

}

