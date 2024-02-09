package com.elijahwaswa.paymentservice.exception;

import com.elijahwaswa.basedomains.exception.ErrorCode;
import com.elijahwaswa.basedomains.exception.ErrorDetails;
import com.elijahwaswa.basedomains.exception.IErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception exception, WebRequest webRequest) {

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorDetails errorDetails=new ErrorDetails();
        errorDetails.setTimestamp(LocalDateTime.now());
        errorDetails.setMessage(exception.getMessage());
        errorDetails.setPath(webRequest.getDescription(false));

        if (exception instanceof IErrorDetails iErrorDetails) {
            errorDetails.setTimestamp(iErrorDetails.getTimeStamp());
            errorDetails.setErrorCode(iErrorDetails.getErrorCode());
            httpStatus = iErrorDetails.getHttStatus();
        }else if(exception instanceof ErrorResponse errorResponse){
            errorDetails.setErrorCode(ErrorCode.ERROR);
            httpStatus =  HttpStatus.valueOf(errorResponse.getStatusCode().value());
        }else {
            errorDetails.setErrorCode(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(errorDetails, httpStatus);
    }

}
