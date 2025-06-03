package com.kss.astrologer.controllers;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kss.astrologer.dto.AstrologerDto;
import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.services.AstrologerService;

@RestController
@RequestMapping("/api/v1/astrologers")
public class AstrologerController {
    
    private static final Logger logger = LoggerFactory.getLogger(AstrologerController.class);

    @Autowired
    private AstrologerService astrologerService;

    @GetMapping
    public ResponseEntity<Object> getAllAstrologers(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                    @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<AstrologerDto> astrologers = astrologerService.getAllAstrologers(page, size);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Successfully fetched astrologers", "astrologers", astrologers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getAstrologerById(@PathVariable UUID id) {
        AstrologerDto astrologer = astrologerService.getAstrologerById(id);
        logger.info("Fetched Astrologer details for ID: {}", id);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Astrologer details fetched successfully", "astrologer", astrologer);
    }
}
