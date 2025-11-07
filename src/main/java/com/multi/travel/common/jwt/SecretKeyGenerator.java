package com.multi.travel.common.jwt;

import java.security.SecureRandom;
import java.util.Base64;

public class SecretKeyGenerator {
    public static void main(String[] args) {
        // 64바이트 길이의 시크릿 키 생성
        byte[] keyBytes = new byte[64];  // 512비트 = 64바이트
        SecureRandom random = new SecureRandom();
        random.nextBytes(keyBytes);

        // Base64로 인코딩
        String encodedSecretKey = Base64.getEncoder().encodeToString(keyBytes);

        // 인코딩된 시크릿 키 출력
        System.out.println("Base64 Encoded Secret Key: " + encodedSecretKey);
    }
}
