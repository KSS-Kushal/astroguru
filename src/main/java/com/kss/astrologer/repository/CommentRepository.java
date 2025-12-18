package com.kss.astrologer.repository;

import com.kss.astrologer.models.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Page<Comment> findByPost_IdOrderByCreatedAtAsc(UUID postId, Pageable pageable);

    List<Comment> findByUser_IdOrderByCreatedAtAsc(UUID userId);

    long countByPost_Id(UUID postId);

    Optional<Comment> findByIdAndUser_Id(UUID id, UUID userId);
}