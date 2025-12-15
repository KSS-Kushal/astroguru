package com.kss.astrologer.controllers;

import com.kss.astrologer.dto.PostDto;
import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.request.PostRequest;
import com.kss.astrologer.security.CustomUserDetails;
import com.kss.astrologer.services.PostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Post", description = "Astrologer Posts and feeds")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/posts")
@Validated
public class PostController {
    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @RequestPart("data") @Valid PostRequest postRequest,
            @RequestPart("text") String text,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
            ) {
        logger.info("text" + text);
        PostDto post = postService.createPost(userDetails.getUserId(), text, images);
        return ResponseHandler.responseBuilder(HttpStatus.CREATED, true, "Post created successfully", "post", post);
    }

    @GetMapping
    public ResponseEntity<Object> getAllPost(
            @RequestParam(defaultValue = "1", required = false) Integer page,
            @RequestParam(defaultValue = "10", required = false) Integer size
    ) {
        Page<PostDto> posts = postService.getAllPost(page, size);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Posts fetched successfully", "posts", posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getPostById(@PathVariable UUID id) {
        PostDto post = postService.getPostById(id);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Post fetched successfully", "post", post);
    }

    @DeleteMapping("image/{id}")
    public ResponseEntity<Object> deletePostImage(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        postService.deletePostImage(userDetails.getUserId(), id);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Post image deleted successfully");
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> updatePost(@PathVariable UUID id,
                                               @AuthenticationPrincipal CustomUserDetails userDetails,
                                               @RequestPart("text") String text,
                                               @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        PostDto post = postService.updatePost(userDetails.getUserId(), id,text, images);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Post updated successfully", "post", post);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletePost(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        postService.deletePost(userDetails.getUserId(), id);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Post deleted successfully");
    }
}
