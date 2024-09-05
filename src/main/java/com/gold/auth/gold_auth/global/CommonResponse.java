package com.gold.auth.gold_auth.global;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class CommonResponse<T> {
    private final HttpStatus httpStatus;
    private final String message;
    private final T data;

    private CommonResponse(HttpStatus status, String message, T data) {
        this.httpStatus = status;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonResponse<T> ok(T data) {
        return ok(null, data);
    }

    public static <T> CommonResponse<T> ok(String message, T data) {
        return new CommonResponse<>(HttpStatus.OK, message, data);
    }

    public static CommonResponse<Void> fail(String message) {
        return fail(message, null);
    }

    public static <T> CommonResponse<T> fail(String message, T data) {
        return new CommonResponse<>(HttpStatus.UNAUTHORIZED, message, data);
    }

}


