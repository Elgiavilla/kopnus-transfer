package com.kopnus.banking.config.exception;

import com.kopnus.banking.shared.constant.ResponseCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ResponseCode responseCode;
    private final String message;

    public BusinessException(ResponseCode responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
        this.message = message;
    }

    public BusinessException(ResponseCode responseCode) {
        this(responseCode, responseCode.getMessage());
    }
}
