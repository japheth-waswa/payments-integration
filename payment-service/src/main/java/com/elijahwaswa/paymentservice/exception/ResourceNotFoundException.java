package com.elijahwaswa.paymentservice.exception;


import com.elijahwaswa.basedomains.exception.ErrorCode;
import com.elijahwaswa.basedomains.exception.IErrorDetails;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ResourceNotFoundException extends RuntimeException implements IErrorDetails {
    private final LocalDateTime timestamp;
    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;


    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(message);
        timestamp = LocalDateTime.now();
        this.errorCode = errorCode;
        httpStatus = HttpStatus.NOT_FOUND;
    }

    @Override
    public LocalDateTime getTimeStamp() {
        return timestamp;
    }

    @Override
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public HttpStatus getHttStatus() {
        return httpStatus;
    }
}
