package com.demo.MixSplit.Exception;
import com.demo.MixSplit.Entity.CustomErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public final ResponseEntity<CustomErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        CustomErrorResponse errorResponse = new CustomErrorResponse(
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Internal server error
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<CustomErrorResponse> handleAllExceptions(
            Exception ex, WebRequest request) {

        CustomErrorResponse errorResponse = new CustomErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred: " + ex.getMessage() + ". Please try again later",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
