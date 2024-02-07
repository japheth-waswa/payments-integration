package com.elijahwaswa.paymentservice.exception;

import com.elijahwaswa.basedomains.exception.ErrorCode;
import com.elijahwaswa.basedomains.exception.ErrorDetails;
import com.elijahwaswa.basedomains.exception.IErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception exception, WebRequest webRequest) {
        ErrorDetails errorDetails;
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        if (exception instanceof IErrorDetails iErrorDetails) {
            errorDetails = new ErrorDetails(
                    iErrorDetails.getTimeStamp(),
                    exception.getMessage(),
                    webRequest.getDescription(false),
                    iErrorDetails.getErrorCode()
            );
            httpStatus = iErrorDetails.getHttStatus();
        } else {
            errorDetails = new ErrorDetails(
                    LocalDateTime.now(),
                    exception.getMessage(),
                    webRequest.getDescription(false),
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        return new ResponseEntity<>(errorDetails, httpStatus);
    }
}
