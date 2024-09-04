package com.gold.auth.gold_auth.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse {

    private final ErrorCode errorCode;
    private final int statusCode;
    private final HttpStatus httpStatus;
    private final String message;

    public ErrorResponse(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.statusCode = errorCode.getHttpStatus().value();
        this.httpStatus = errorCode.getHttpStatus();
        this.message = message;
    }
}
