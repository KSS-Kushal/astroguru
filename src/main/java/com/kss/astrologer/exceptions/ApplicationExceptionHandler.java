package com.kss.astrologer.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.kss.astrologer.handler.ResponseHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class ApplicationExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleInvalidArgument(MethodArgumentNotValidException ex){
        Map<String, String> errors = ex.getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                    FieldError::getField,
                    LinkedHashMap::new, // keep insertion order
                    Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
            )).entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> String.join(" and ", entry.getValue()) // combine messages with "and"
            ));
        return ResponseHandler.responseBuilder(HttpStatus.BAD_REQUEST, false, "Bad Request", "errors", errors);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return ResponseHandler.responseBuilder(HttpStatus.PAYLOAD_TOO_LARGE, false, "File size exceeds limit");
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AuthorizationDeniedException ex) {
        return ResponseHandler.responseBuilder(HttpStatus.FORBIDDEN, false, "Access Denied");
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(CustomException ex, WebRequest request) {
        logger.warn("CustomException at [{}]: {}", request.getDescription(false), ex.getMessage());
        return ResponseHandler.responseBuilder(ex.getStatus(), false, ex.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        logger.info("UsernameNotFoundException at [{}]: {}", request.getDescription(false), ex.getMessage());
        return ResponseHandler.responseBuilder(HttpStatus.UNAUTHORIZED, false, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllException(Exception ex, WebRequest request) {
        logger.error("Unexpected error at [{}]: {}", request.getDescription(false), ex.getMessage(), ex);
        return ResponseHandler.responseBuilder(HttpStatus.INTERNAL_SERVER_ERROR, false, "Internal server error");
    }
}
