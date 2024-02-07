package com.elijahwaswa.basedomains.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public interface IErrorDetails {
    LocalDateTime getTimeStamp();
    ErrorCode getErrorCode();
    HttpStatus getHttStatus();
}
