package com.kss.astrologer.repository;

import com.kss.astrologer.dto.LikeUserDto;
import com.kss.astrologer.models.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {
    boolean existsByUser_IdAndPost_Id(UUID userId, UUID postId);

    void deleteByUser_IdAndPost_Id(UUID userId, UUID postId);

    long countByPost_Id(UUID postId);

    @Query("""
        select new com.kss.astrologer.dto.LikeUserDto(
            u.id, u.name
        )
        from Like l
        join l.user u
        where l.post.id = :postId
    """)
    List<LikeUserDto> findLikedUsers(UUID postId);
}