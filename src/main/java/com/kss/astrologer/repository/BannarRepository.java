package com.kss.astrologer.repository;

import com.kss.astrologer.models.Bannar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BannarRepository extends JpaRepository<Bannar, UUID> {
}
