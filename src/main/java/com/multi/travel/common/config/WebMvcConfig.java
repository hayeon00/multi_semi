package com.multi.travel.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : WebMvcConfig
 * @since : 2025. 11. 8. 토요일
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${image.review.add-resource-handler}")
    private String reviewHandler;

    @Value("${image.review.add-resource-locations}")
    private String reviewLocation;

    @Value("${image.member.add-resource-handler}")
    private String memberHandler;

    @Value("${image.member.add-resource-locations}")
    private String memberLocation;

    @Value("${image.acc.add-resource-handler}")
    private String accHandler;

    @Value("${image.acc.add-resource-locations}")
    private String accLocation;

    @Value("${image.tourspot.add-resource-handler}")
    private String tourspotHandler;

    @Value("${image.tourspot.add-resource-locations}")
    private String tourspotLocation;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/reviewImages/**")
                .addResourceLocations("file:/Users/rlagkdus/uploads/reviewImages/");

        registry.addResourceHandler(memberHandler)
                .addResourceLocations(memberLocation);

        registry.addResourceHandler(accHandler)
                .addResourceLocations(accLocation);

        registry.addResourceHandler(tourspotHandler)
                .addResourceLocations(tourspotLocation);
    }


}

