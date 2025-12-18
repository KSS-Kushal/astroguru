package com.kss.astrologer.repository;

import com.kss.astrologer.models.AstrologerDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AstrologerDeviceTokenRepository extends JpaRepository<AstrologerDeviceToken, UUID> {
    Optional<AstrologerDeviceToken> findByDeviceToken(String deviceToken);
    List<AstrologerDeviceToken> findByUserId(UUID astrologerId);
    @Query("""
        SELECT dt.deviceToken
        FROM AstrologerDeviceToken dt
        WHERE dt.userId <> :userId
    """)
    List<String> findAllTokensExceptUser(@Param("userId") UUID userId);
}
