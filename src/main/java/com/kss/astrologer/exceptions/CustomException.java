package com.kss.astrologer.exceptions;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException{
    private HttpStatus status;

    public CustomException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public CustomException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public CustomException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
