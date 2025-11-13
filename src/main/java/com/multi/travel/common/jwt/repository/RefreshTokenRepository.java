package com.multi.travel.common.jwt.repository;


import com.multi.travel.common.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByLoginId(String userId);

    @Modifying
    @Transactional
    void deleteByLoginId(String userId);
}