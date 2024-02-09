package com.elijahwaswa.basedomains.exception;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class ErrorDetails{
    private LocalDateTime timestamp;
    private String message;
    private String path;
    private ErrorCode errorCode;
}
