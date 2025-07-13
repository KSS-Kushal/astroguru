package com.kss.astrologer.controllers;

import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.models.Bannar;
import com.kss.astrologer.services.BannarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bannar")
public class BannarController {

    @Autowired
    private BannarService bannarService;

    @GetMapping
    public ResponseEntity<Object> getAllBannars() {
        List<Bannar> bannars = bannarService.getBannar();
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Bannar Fetched Successfully", "bannars", bannars);
    }

}
