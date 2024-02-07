package com.elijahwaswa.basedomains.exception;

import java.time.LocalDateTime;

public record ErrorDetails(LocalDateTime timestamp, String message,String path, ErrorCode errorCode) {
}
