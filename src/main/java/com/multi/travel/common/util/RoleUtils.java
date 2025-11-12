package com.multi.travel.common.util;

/*
 * Please explain the class!!!
 *
 * @filename    : GetRole
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 11. 화요일
 */


import com.multi.travel.auth.dto.CustomUser;

public class RoleUtils {
    public static final String ADMIN = "ROLE_ADMIN";
    public static final String USER = "ROLE_USER";

    public static boolean hasRole(CustomUser customUser, String roleName) {
        if (customUser == null || customUser.getAuthorities() == null) {
            return false;
        }
        return customUser.getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleName));
    }
}
