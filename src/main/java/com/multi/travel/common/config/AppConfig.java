package com.multi.travel.common.config;

/*
 * Please explain the class!!!
 *
 * @filename    : AppConfig
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 12. 수요일
 */


import org.springframework.beans.factory.annotation.Value;

public class AppConfig {
    @Value("${kakao.map.api-key}")
    private String kakaoMapApiKey;

    public String getKakaoMapApiKey() {
        return kakaoMapApiKey;
    }
}
