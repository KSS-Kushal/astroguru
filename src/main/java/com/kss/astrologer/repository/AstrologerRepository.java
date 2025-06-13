package com.kss.astrologer.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kss.astrologer.models.AstrologerDetails;

@Repository
public interface AstrologerRepository extends JpaRepository<AstrologerDetails, UUID> {

    Optional<AstrologerDetails> findByUserId(UUID id);
    
}
