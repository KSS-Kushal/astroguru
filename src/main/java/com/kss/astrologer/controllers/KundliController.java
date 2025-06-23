package com.kss.astrologer.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.request.KundliRequest;
import com.kss.astrologer.services.astro.KundliService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/kundli")
public class KundliController {
    
    private static final Logger logger = LoggerFactory.getLogger(KundliController.class);

    @Autowired
    private KundliService kundliService;

    @PostMapping
    public ResponseEntity<Object> generateKundli(@RequestBody @Valid KundliRequest kundliRequest) {
        Object kundli = kundliService.getKundli(kundliRequest);
        if (kundli == null) {
            logger.error("Kundli generation failed for request: {}", kundliRequest);
            return ResponseHandler.responseBuilder(HttpStatus.BAD_REQUEST, false,"Kundli generation failed");
        }
        logger.info("Kundli generated successfully for request: {}", kundliRequest);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Kundli generated successfully", kundli);
    }
}
