package com.kss.astrologer.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHandler {
    public static ResponseEntity<Object> responseBuilder(HttpStatus httpStatus, boolean success, String msg, String key, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("msg", msg);
        response.put(key, data);

        return new ResponseEntity<>(response, httpStatus);
    }

    public static ResponseEntity<Object> responseBuilder(HttpStatus httpStatus, boolean success, String msg, String key, Object data, int currentPage, int totalPages, long totalItems, boolean isLastpage) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("msg", msg);
        response.put(key, data);
        response.put("currentPage", currentPage);
        response.put("totalPages", totalPages);
        response.put("totalItems", totalItems);
        response.put("isLastPage", isLastpage);

        return new ResponseEntity<>(response, httpStatus);
    }

    public static ResponseEntity<Object> responseBuilder(HttpStatus httpStatus, boolean success, String msg, String key, Page<?> pageData) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("msg", msg);
        response.put(key, pageData.getContent());
        response.put("currentPage", pageData.getNumber() + 1);
        response.put("totalPages", pageData.getTotalPages());
        response.put("totalItems", pageData.getTotalElements());
        response.put("isLastPage", pageData.isLast());

        return new ResponseEntity<>(response, httpStatus);
    }

    public static ResponseEntity<Object> responseBuilder(HttpStatus httpStatus, boolean success, String msg, String key, Object data, String token) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("msg", msg);
        response.put(key, data);
        response.put("token", token);

        return new ResponseEntity<>(response, httpStatus);
    }

    
    public static ResponseEntity<Object> responseBuilder(HttpStatus httpStatus, boolean success, String msg, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("msg", msg);
        response.put("data", data);

        return new ResponseEntity<>(response, httpStatus);
    }


    public static ResponseEntity<Object> responseBuilder(HttpStatus httpStatus, boolean success, String msg) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("msg", msg);

        return new ResponseEntity<>(response, httpStatus);
    }
}
