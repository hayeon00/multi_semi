package com.multi.travel.common.config;

/*
 * Please explain the class!!!
 *
 * @filename    : AppConfig
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 12. 수요일
 */


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@Getter
public class AppConfig {
    @Value("${kakao.map.api-key}")
    private String kakaoMapApiKey;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
