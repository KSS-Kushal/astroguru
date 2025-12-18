package com.kss.astrologer.controllers;

import java.util.List;
import java.util.UUID;

import com.kss.astrologer.request.OnlineStatusRequest;
import com.kss.astrologer.request.UpdateAstrologerRequest;
import com.kss.astrologer.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.kss.astrologer.dto.AstrologerDto;
import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.request.AstrologerRequest;
import com.kss.astrologer.services.AstrologerService;
import com.kss.astrologer.services.aws.S3Service;

import jakarta.validation.Valid;

@Tag(name = "Astrologer")
@RestController
@RequestMapping("/api/v1/astrologers")
public class AstrologerController {

    private static final Logger logger = LoggerFactory.getLogger(AstrologerController.class);

    @Autowired
    private AstrologerService astrologerService;

    @Autowired
    private S3Service s3Service;

    @GetMapping
    public ResponseEntity<Object> getAllAstrologers(@RequestParam(defaultValue = "1", required = false) Integer page,
                                                    @RequestParam(defaultValue = "10", required = false) Integer size,
                                                    @RequestParam(required = false) String search,
                                                    @RequestParam(required = false) String sort) {
        Page<AstrologerDto> astrologers = astrologerService.getAllAstrologers(page, size, search, sort);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Successfully fetched astrologers", "astrologers",
                astrologers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getAstrologerById(@PathVariable UUID id) {
        AstrologerDto astrologer = astrologerService.getAstrologerById(id);
        logger.info("Fetched Astrologer details for ID: {}", id);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Astrologer details fetched successfully",
                "astrologer", astrologer);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> updateAstrologerById(
            @PathVariable UUID id,
            @RequestPart("data") @Valid UpdateAstrologerRequest astrologerRequest,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        logger.info(astrologerRequest.toString());
        String imgUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imgUrl = s3Service.uploadFile(imageFile, "astrologers");
        }
        AstrologerDto updatedAstrologer = astrologerService.updateAstrologer(astrologerRequest, id, imgUrl);
        logger.info("Updated Astrologer details for ID: {}", id);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Astrologer details updated successfully",
                "astrologer", updatedAstrologer);
    }

    @GetMapping("/online/list")
    public ResponseEntity<Object> getOnlineAstrologers() {
        List<AstrologerDto> astrologers = astrologerService.getOnlineAstrologer();
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Online Astrologer fetched successfully", "astrologers", astrologers);
    }

    @PostMapping("/change-online")
    public ResponseEntity<Object> changeOnlineStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody OnlineStatusRequest request
            ) {
        AstrologerDto astrologer = astrologerService.changeAstrologerOnlineStatus(userDetails.getUserId(), request);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Online status changed successfully", "astrologer", astrologer);
    }
}
