package com.kss.astrologer.repository;

import com.kss.astrologer.models.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {
    // Check if a user has liked a post
    boolean existsByUserIdAndPostId(UUID userId, UUID postId);

    // All likes here
    List<Like> findByPostId(UUID postId);

    // All likes by user
    List<Like> findByUserId(UUID userId);

    // Remove like
    void deleteByUserIdAndPostId(UUID userId, UUID postId);


    @Query("SELECT COUNT(l) FROM Like l WHERE l.postId = :postId")
    long countByPostId(@Param("postId") UUID postId); //count likes
}