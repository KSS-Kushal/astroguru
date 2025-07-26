package com.kss.astrologer.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kss.astrologer.models.AstrologerDetails;

@Repository
public interface AstrologerRepository extends JpaRepository<AstrologerDetails, UUID> {

    Optional<AstrologerDetails> findByUserId(UUID id);

    @Query("SELECT a FROM AstrologerDetails a WHERE " +
            "LOWER(a.user.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.about) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.expertise) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<AstrologerDetails> searchAstrologers(@Param("search") String search, Pageable pageable);
}
