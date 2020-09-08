package com.adapter.exception;

import lombok.Getter;

public class InternalException extends Exception {
    private static final long serialVersionUID = -5143177235116707114L;
    @Getter
    private final ErrorCode errorCode;

    public InternalException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public InternalException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
