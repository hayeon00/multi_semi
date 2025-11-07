package com.multi.travel.common.jwt.repository;


import com.multi.travel.common.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUserId(String userId);

    void deleteByUserId(String userId);
}