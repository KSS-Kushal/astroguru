package com.kss.astrologer.controllers;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kss.astrologer.dto.AstrologerDto;
import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.request.AstrologerRequest;
import com.kss.astrologer.services.AstrologerService;
import com.kss.astrologer.services.aws.S3Service;

import jakarta.validation.Valid;

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

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> updateAstrologerById(
            @PathVariable UUID id,
            @RequestPart("data") @Valid AstrologerRequest astrologerRequest,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        String imgUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imgUrl = s3Service.uploadFile(imageFile, "astrologers");
        }
        AstrologerDto updatedAstrologer = astrologerService.updateAstrologer(astrologerRequest, id, imgUrl);
        logger.info("Updated Astrologer details for ID: {}", id);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Astrologer details updated successfully",
                "astrologer", updatedAstrologer);
    }
}
