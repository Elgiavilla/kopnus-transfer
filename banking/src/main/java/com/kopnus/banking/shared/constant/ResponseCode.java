package com.kopnus.banking.shared.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResponseCode {
    SUCCESS             ("SUCCESS",             "Success",                      HttpStatus.OK),
    BAD_REQUEST         ("BAD_REQUEST",         "Invalid input body",           HttpStatus.BAD_REQUEST),
    DATA_NOT_EXIST      ("NOT_FOUND",           "Data not found",               HttpStatus.NOT_FOUND),
    DUPLICATE_DATA      ("CONFLICT",            "Duplicate data",               HttpStatus.CONFLICT),
    INSUFFICIENT_FUNC  ("INSUFFICIENT_FUNC",  "Insufficient stock",           HttpStatus.CONFLICT),
    INVALID_STATE       ("INVALID_STATE",       "Operation not allowed",        HttpStatus.CONFLICT),
    CONCURRENT_UPDATE   ("CONCURRENT_UPDATE",   "Resource was modified, retry", HttpStatus.CONFLICT),
    SYSTEM_ERROR        ("INTERNAL_SERVER_ERROR","Contact our team",            HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED        ("UNAUTHORIZED","Invalid username and password",            HttpStatus.UNAUTHORIZED),
    FORBIDDEN        ("FORBIDDEN","",            HttpStatus.FORBIDDEN),
    UNPROCESSABLE_ENTITY        ("UNPROCESSABLE_ENTITY","",            HttpStatus.UNPROCESSABLE_ENTITY),
    TOO_MANY_REQUESTS        ("TOO_MANY_REQUESTS","",            HttpStatus.TOO_MANY_REQUESTS);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ResponseCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}