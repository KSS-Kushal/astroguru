package com.kss.astrologer.services;

import com.kss.astrologer.dto.PostDto;
import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.models.AstrologerDetails;
import com.kss.astrologer.models.Post;
import com.kss.astrologer.models.PostImage;
import com.kss.astrologer.repository.AstrologerRepository;
import com.kss.astrologer.repository.PostImageRepository;
import com.kss.astrologer.repository.PostRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    @Autowired
    private PostRepository postRepository;

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

        return new PostDto(postRepository.save(post));
    }

    public Page<PostDto> getAllPost(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.DESC, "createdAt");
        Page<Post> posts = postRepository.findAll(pageable);
        return posts.map(PostDto::new);
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

        return new PostDto(postRepository.save(post));
    }

    public PostDto getPostById(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Post not found"));
        return new PostDto(post);
    }

    public void deletePost(UUID userId, UUID postId) {
        Post post = postRepository.findByIdAndAstrologer_User_Id(postId, userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Post not found or not accessible"));
        postRepository.delete(post);
    }
}
