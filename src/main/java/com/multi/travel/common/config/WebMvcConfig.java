package com.multi.travel.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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

    @Value("${image.default.add-resource-handler}")
    private String defaultHandler;

    @Value("${image.default.add-resource-locations}")
    private String defaultLocation;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(reviewHandler)
                .addResourceLocations(reviewLocation);

        registry.addResourceHandler(memberHandler)
                .addResourceLocations(memberLocation);

        registry.addResourceHandler(accHandler)
                .addResourceLocations(accLocation);

        registry.addResourceHandler(tourspotHandler)
                .addResourceLocations(tourspotLocation);

        registry.addResourceHandler(defaultHandler)
                .addResourceLocations(defaultLocation);
    }
}
