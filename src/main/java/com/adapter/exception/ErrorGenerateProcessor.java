package com.adapter.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ErrorGenerateProcessor implements Processor {
    public static final String ERROR_CODE_HEADER = "application_error_code";
    public static final String ERROR_MESSAGE_HEADER = "application_error_message";

    @Override
    public void process(Exchange exchange) throws Exception {
        Exception exception = getException(exchange);
        if (exception != null) {
            throw getException(
                    exchange.getIn().getHeader(ERROR_CODE_HEADER, ErrorCode.class),
                    exchange.getIn().getHeader(ERROR_MESSAGE_HEADER, String.class),
                    exception);
        }
    }

    static Exception getException(Exchange exchange) {
        return Optional.ofNullable(exchange.getException())
                .orElse(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class));
    }

    public static InternalException getException(ErrorCode errorCode, String errorMessage) {
        return new InternalException(errorCode, getInternalExceptionMessage(errorCode, errorMessage, null));
    }

    public static InternalException getException(ErrorCode errorCode, String errorMessage, Throwable e) {
        return new InternalException(errorCode, getInternalExceptionMessage(errorCode, errorMessage, e), e);
    }

    private static String getInternalExceptionMessage(ErrorCode errorCode, String errorMessage, Throwable e) {
        return "Internal exception: " + getErrorCode(errorCode) + getErrorMessage(errorMessage, e);
    }

    private static String getErrorCode(ErrorCode errorCode) {
        return "code='" + (errorCode == null ? ErrorCode.UNEXPECTED : errorCode.name());
    }

    private static String getErrorMessage(String errorMessage, Throwable e) {
        errorMessage = "', message ='" + errorMessage + '\'';
        return e == null ? errorMessage : errorMessage + " cause='" + e.getMessage() + "'";
    }
}
