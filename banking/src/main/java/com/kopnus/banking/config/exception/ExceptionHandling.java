package com.kopnus.banking.config.exception;

import com.kopnus.banking.shared.base.BaseResponse;
import com.kopnus.banking.shared.base.CommonResponse;
import com.kopnus.banking.shared.base.GlobalBadRequestResponse;
import com.kopnus.banking.shared.constant.ResponseCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ExceptionHandling {

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity businessLogicException(BusinessException ble) {
        log.info("BusinessLogicException {}", ble.getMessage());
        BaseResponse<?> response = CommonResponse.constructResponse(
                ble.getResponseCode().getCode(),
                ble.getMessage(),
                null,
                null
        );
        return new ResponseEntity<>(response, HttpStatus.valueOf(ble.getResponseCode().getCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        log.warn("Argument Exception Handler", ex);
        List<GlobalBadRequestResponse> payloadExceptions = new ArrayList<>();
        String message = null;
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            if(message == null){
                message = error.getDefaultMessage();
            }
            payloadExceptions.add(new GlobalBadRequestResponse(
                    error.getField(),
                    error.getDefaultMessage()));
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            if(message == null){
                message = error.getDefaultMessage();
            }
            payloadExceptions.add(new GlobalBadRequestResponse(error.getObjectName(), error.getDefaultMessage()));
        }

        BaseResponse<?> response = CommonResponse.constructResponse(
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                "",
                null,
                payloadExceptions
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<Object> handleUnreadable(HttpMessageNotReadableException ex) {
        BaseResponse<?> response = CommonResponse.constructResponse(
                ResponseCode.BAD_REQUEST.getCode(),
                "Malformed request body or invalid enum value",
                null, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<BaseResponse<Object>> handleConstraintViolation(
            ConstraintViolationException ex) {

        log.warn("Constraint violation: {}", ex.getMessage());

        List<GlobalBadRequestResponse> errors = ex.getConstraintViolations().stream()
                .map(v -> new GlobalBadRequestResponse(
                        v.getPropertyPath().toString(), v.getMessage()))
                .toList();

        BaseResponse<Object> response = CommonResponse.constructResponse(
                ResponseCode.BAD_REQUEST.getCode(),
                "Validation failed",
                null,
                errors
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<BaseResponse<Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String message = String.format("Parameter '%s' has invalid value: %s",
                ex.getName(), ex.getValue());
        log.warn(message);

        BaseResponse<Object> response = CommonResponse.constructResponse(
                ResponseCode.BAD_REQUEST.getCode(), message, null, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<BaseResponse<Object>> handleMissingParam(
            MissingServletRequestParameterException ex) {

        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        log.warn(message);

        BaseResponse<Object> response = CommonResponse.constructResponse(
                ResponseCode.BAD_REQUEST.getCode(), message, null, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    protected ResponseEntity<BaseResponse<Object>> handleOptimisticLock(
            OptimisticLockingFailureException ex) {

        log.warn("Optimistic lock conflict: {}", ex.getMessage());

        BaseResponse<Object> response = CommonResponse.constructResponse(
                ResponseCode.CONCURRENT_UPDATE.getCode(),
                "Stock was modified by another transaction. Please retry.",
                null, null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    protected ResponseEntity<BaseResponse<Object>> handlePessimisticLock(
            PessimisticLockingFailureException ex) {

        log.warn("Pessimistic lock failure: {}", ex.getMessage());

        BaseResponse<Object> response = CommonResponse.constructResponse(
                ResponseCode.CONCURRENT_UPDATE.getCode(),
                "Could not acquire lock on stock. Please retry.",
                null, null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<BaseResponse<Object>> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        String rootCause = ex.getMostSpecificCause().getMessage();
        log.error("Data integrity violation: {}", rootCause);

        String message;
        ResponseCode code;

        if (rootCause != null && rootCause.toLowerCase().contains("duplicate")) {
            code = ResponseCode.DUPLICATE_DATA;
            message = "A record with the same unique value already exists";
        } else {
            code = ResponseCode.BAD_REQUEST;
            message = "Operation violates a data constraint";
        }

        BaseResponse<Object> response = CommonResponse.constructResponse(
                code.getCode(), message, null, null);
        return new ResponseEntity<>(response, code.getHttpStatus());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<BaseResponse<Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {

        BaseResponse<Object> response = CommonResponse.constructResponse(
                "METHOD_NOT_ALLOWED", ex.getMessage(), null, null);
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<BaseResponse<Object>> handleNotFound(NoHandlerFoundException ex) {
        BaseResponse<Object> response = CommonResponse.constructResponse(
                ResponseCode.DATA_NOT_EXIST.getCode(),
                "Endpoint not found: " + ex.getRequestURL(),
                null, null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<BaseResponse<Object>> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);   // stack trace penuh ke log

        BaseResponse<Object> response = CommonResponse.constructResponse(
                ResponseCode.SYSTEM_ERROR.getCode(),
                ResponseCode.SYSTEM_ERROR.getMessage(),   // "Contact our team" — generik
                null, null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
