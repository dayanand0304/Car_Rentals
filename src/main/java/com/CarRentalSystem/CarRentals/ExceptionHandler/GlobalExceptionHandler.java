package com.CarRentalSystem.CarRentals.ExceptionHandler;

import com.CarRentalSystem.CarRentals.CustomExceptions.Cars.*;
import com.CarRentalSystem.CarRentals.CustomExceptions.Customers.*;
import com.CarRentalSystem.CarRentals.CustomExceptions.Rentals.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 404: NOT FOUND
    @ExceptionHandler({
            CarNotFoundException.class,
            CustomerNotFoundException.class,
            RentalNotFoundException.class,
            RentalNotFoundByCustomerIdException.class,
            RentalNotFoundByCarIdException.class,
            RefreshTokenNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex,
                                                        HttpServletRequest request) {

        log.warn("404 NOT FOUND at [{}]: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // 403: FORBIDDEN
    @ExceptionHandler({
            AccessDeniedException.class,
            CustomerInActiveException.class,
            RefreshTokenExpireException.class
    })
    public ResponseEntity<ErrorResponse> handleForbidden(RuntimeException ex,
                                                         HttpServletRequest request) {

        log.warn("403 FORBIDDEN at [{}]: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // 400: BAD REQUEST
    @ExceptionHandler({
            CustomerIdRequiredException.class,
            DurationException.class,
            RentalTypeException.class,
            RegistrationNumberFormatException.class,
            DamagedFeeNullException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex,
                                                          HttpServletRequest request) {

        log.warn("400 BAD REQUEST at [{}]: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // 410: GONE
    @ExceptionHandler(CarNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleGone(RuntimeException ex,
                                                    HttpServletRequest request) {

        log.warn("410 GONE at [{}]: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(
                HttpStatus.GONE,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // 400: VALIDATION FAILURE
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException ex,
                                                               HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        log.warn("VALIDATION FAILED at [{}]: {}", request.getRequestURI(), message);

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request.getRequestURI()
        );
    }

    // 409: CONFLICT
    @ExceptionHandler({
            CarNotAvailableException.class,
            RentalAlreadyReturnedException.class,
            RentalAlreadyCancelledException.class,
            CustomerAlreadyExistsException.class,
            CannotCancelException.class,
            CarAlreadyExistsException.class,
            CustomerAlreadyDeletedException.class,
            InvalidRentalStateException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex,
                                                        HttpServletRequest request) {

        log.warn("409 CONFLICT at [{}]: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // 500: INTERNAL SERVER ERROR
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error(
                "Unhandled Exception | Path: {} | Time: {}",
                request.getRequestURI(),
                LocalDateTime.now(),
                ex
        );

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected server error",
                request.getRequestURI()
        );
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status,
                                                             String message,
                                                             String path) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.name(),
                message,
                path
        );

        return ResponseEntity.status(status).body(error);
    }
}
