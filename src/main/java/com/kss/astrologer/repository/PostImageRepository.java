package com.kss.astrologer.repository;

import com.kss.astrologer.models.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, UUID> {
    Optional<PostImage> findByIdAndPost_Astrologer_User_Id(UUID postImageId, UUID userId);
}
