package com.kss.astrologer.repository;

import com.kss.astrologer.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    Optional<Post> findByIdAndAstrologer_User_Id(UUID postId, UUID userId);

    @Query("SELECT COUNT(l) FROM Like l WHERE l.postId = :postId")
    long countLikesByPostId(@Param("postId") UUID postId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId")
    long countCommentsByPostId(@Param("postId") UUID postId);
}