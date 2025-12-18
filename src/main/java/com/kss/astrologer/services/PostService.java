package com.kss.astrologer.services;

import com.kss.astrologer.dto.CommentDTO;
import com.kss.astrologer.dto.LikeDTO;
import com.kss.astrologer.dto.PostDto;
import com.kss.astrologer.events.PostCreatedEvent;
import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.models.*;
import com.kss.astrologer.repository.*;
import com.kss.astrologer.services.aws.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private UserRepository userRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private AstrologerRepository astrologerRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

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

        eventPublisher.publishEvent(
                new PostCreatedEvent(
                        savedPost.getId(),
                        savedPost.getAstrologer().getId()
                )
        );

        return new PostDto(savedPost);
    }

    public Page<PostDto> getAllPost(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.DESC, "createdAt");
        Page<Post> posts = postRepository.findAll(pageable);
        return posts.map(post -> {
            PostDto dto = new PostDto(post);
            dto.setLikeCount(likeRepository.countByPost_Id(post.getId()));
            dto.setCommentCount(commentRepository.countByPost_Id(post.getId()));
            return dto;
        });
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
        PostDto dto = new PostDto(savedPost);
        dto.setLikeCount(likeRepository.countByPost_Id(savedPost.getId()));
        dto.setCommentCount(commentRepository.countByPost_Id(savedPost.getId()));
        return dto;
    }

    public PostDto getPostById(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Post not found"));
        PostDto dto = new PostDto(post);
        dto.setLikeCount(likeRepository.countByPost_Id(id));
        dto.setCommentCount(commentRepository.countByPost_Id(id));
        return dto;
    }

    @Transactional
    public void deletePost(UUID userId, UUID postId) {
        Post post = postRepository.findByIdAndAstrologer_User_Id(postId, userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Post not found or not accessible"));
        postRepository.delete(post);
    }

    @Transactional
    public LikeDTO toggleLike(UUID userId, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Post not found"));

        if (likeRepository.existsByUser_IdAndPost_Id(userId, postId)) {
            likeRepository.deleteByUser_IdAndPost_Id(userId, postId);
            return null;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found"));

        Like like = Like.builder().user(user).post(post).build();

        Like savedLike = likeRepository.save(like);
        return new LikeDTO(savedLike);
    }

    @Transactional
    public CommentDTO addComment(UUID userId, UUID postId, String body) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Post not found"));
        if (body == null || body.trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Comment body required");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found"));
        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .body(body.trim())
                .build();
        Comment savedComment = commentRepository.save(comment);
        return new CommentDTO(savedComment);
    }

    @Transactional
    public void deleteComment(UUID userId, UUID commentId) {
        Comment comment = commentRepository.findByIdAndUser_Id(commentId, userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Comment not found or unauthorized"));
        commentRepository.delete(comment);
    }

    public Page<CommentDTO> getComments(UUID postId, Integer page, Integer size) {
        if (!postRepository.existsById(postId)) {
            throw new CustomException(HttpStatus.NOT_FOUND, "Post not found");
        }
        Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.ASC, "createdAt");
        Page<Comment> comments = commentRepository.findByPost_Id(postId, pageable);
        return comments.map(CommentDTO::new);
    }
}