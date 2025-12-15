package com.kss.astrologer.repository;

import com.kss.astrologer.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByPostIdOrderByCreatedAtAsc(UUID postId);

    List<Comment> findByUserIdOrderByCreatedAtAsc(UUID userId);

    // Count commentss
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId")
    long countByPostId(@Param("postId") UUID postId);
}