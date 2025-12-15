package com.kss.astrologer.services;

import com.kss.astrologer.dto.CommentDTO;
import com.kss.astrologer.dto.LikeDTO;
import com.kss.astrologer.dto.PostDto;
import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.models.*;
import com.kss.astrologer.repository.*;
import com.kss.astrologer.services.aws.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private AstrologerRepository astrologerRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Transactional
    public PostDto createPost(UUID userId, String text, List<MultipartFile> images) {
        AstrologerDetails astrologer = astrologerRepository.findByUserId(userId)
                .orElseThrow(()-> new CustomException(HttpStatus.NOT_FOUND, "Astrologer details not found"));
        Post post = Post.builder()
                .text(text)
                .astrologer(astrologer).build();

        List<PostImage> postImages = new ArrayList<>();
        if(images != null) {
            for (MultipartFile file : images) {
                if(file == null || file.isEmpty()) continue;
                String location = "post/" + userId;
                String url = s3Service.uploadFile(file, location);
                PostImage image = new PostImage();
                image.setImagUrl(url);
                image.setPost(post);

                postImages.add(image);
            }
        }

        post.setImages(postImages);

        Post savedPost = postRepository.save(post);
        return buildPostDtoWithCounts(savedPost);
    }

    public Page<PostDto> getAllPost(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.DESC, "createdAt");
        Page<Post> posts = postRepository.findAll(pageable);
        return posts.map(this::buildPostDtoWithCounts);
    }

    @Transactional
    public void deletePostImage(UUID userId, UUID postImageId) {
        PostImage image = postImageRepository.findByIdAndPost_Astrologer_User_Id(postImageId, userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Image not found or not accessible"));
        s3Service.deleteFileByUrl(image.getImagUrl());
        postImageRepository.delete(image);
    }

    @Transactional
    public PostDto updatePost(UUID userId, UUID postId,  String text, List<MultipartFile> images) {
        Post post = postRepository.findByIdAndAstrologer_User_Id(postId, userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Post not found or not accessible"));

        List<PostImage> postImages = post.getImages();
        if(images != null) {
            for (MultipartFile file : images) {
                if(file == null || file.isEmpty()) continue;
                String location = "post/" + userId;
                String url = s3Service.uploadFile(file, location);
                PostImage image = new PostImage();
                image.setImagUrl(url);
                image.setPost(post);

                postImages.add(image);
            }
        }

        if(images != null && !images.isEmpty()) post.setImages(postImages);
        if(text != null) post.setText(text);

        Post savedPost = postRepository.save(post);
        return buildPostDtoWithCounts(savedPost);
    }

    public PostDto getPostById(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Post not found"));
        return buildPostDtoWithCounts(post);
    }

    public void deletePost(UUID userId, UUID postId) {
        Post post = postRepository.findByIdAndAstrologer_User_Id(postId, userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Post not found or not accessible"));
        postRepository.delete(post);
    }

    @Transactional
    public LikeDTO likePost(UUID userId, UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new CustomException(HttpStatus.NOT_FOUND, "Post not found");
        }

        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new CustomException(HttpStatus.CONFLICT, "Already liked");
        }
        Like like = Like.builder()
                .userId(userId)
                .postId(postId)
                .createdAt(LocalDateTime.now())
                .build();
        Like savedLike = likeRepository.save(like);
        return LikeDTO.builder()
                .id(savedLike.getId())
                .userId(savedLike.getUserId())
                .postId(savedLike.getPostId())
                .createdAt(savedLike.getCreatedAt())
                .build();
    }

    @Transactional
    public void unlikePost(UUID userId, UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new CustomException(HttpStatus.NOT_FOUND, "Post not found");
        }
        if (!likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new CustomException(HttpStatus.NOT_FOUND, "Like not found");
        }
        likeRepository.deleteByUserIdAndPostId(userId, postId);
    }

    @Transactional
    public CommentDTO addComment(UUID userId, UUID postId, String body) {
        if (!postRepository.existsById(postId)) {
            throw new CustomException(HttpStatus.NOT_FOUND, "Post not found");
        }
        if (body == null || body.trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Comment body required");
        }
        Comment comment = Comment.builder()
                .userId(userId)
                .postId(postId)
                .body(body)
                .createdAt(LocalDateTime.now())
                .build();
        Comment savedComment = commentRepository.save(comment);
        return CommentDTO.builder()
                .id(savedComment.getId())
                .userId(savedComment.getUserId())
                .postId(savedComment.getPostId())
                .body(savedComment.getBody())
                .createdAt(savedComment.getCreatedAt())
                .build();
    }

    @Transactional
    public void deleteComment(UUID userId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (!comment.getUserId().equals(userId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "Cannot delete others' comments");
        }
        commentRepository.deleteById(commentId);
    }

    private PostDto buildPostDtoWithCounts(Post post) {
        PostDto dto = new PostDto(post);
        dto.setLikeCount(likeRepository.countByPostId(post.getId()));
        dto.setCommentCount(commentRepository.countByPostId(post.getId()));
        return dto;
    }
}
