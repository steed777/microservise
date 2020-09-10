package com.adapter.exception;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.DefaultMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.adapter.exception.ErrorGenerateProcessor.ERROR_CODE_HEADER;
import static com.adapter.exception.ErrorGenerateProcessor.ERROR_MESSAGE_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ErrorGenerateProcessorTest {
    private ErrorGenerateProcessor processor;

    @BeforeEach
    public void setUp() {
        processor = new ErrorGenerateProcessor();
    }

    @Test
    void processNoExceptionTest() {
        CamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(new DefaultMessage(camelContext));

        try {
            processor.process(exchange);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void processExceptionWithoutHeadersTest() throws Exception {
        DefaultCamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(new DefaultMessage(camelContext));

        exchange.setException(new Exception("some exception"));

        try {
            processor.process(exchange);
        } catch (InternalException e) {
            assertEquals(
                    "Internal exception: code='UNEXPECTED', message ='null' cause='some exception'",
                    e.getMessage());
        }
    }

    @Test
    void processExceptionWithHeadersTest() throws Exception {
        DefaultCamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(new DefaultMessage(camelContext));
        exchange.setProperty(Exchange.EXCEPTION_CAUGHT, new Exception());
        exchange.getIn().setHeader(ERROR_CODE_HEADER, ErrorCode.EXTERNAL_SERVICE);
        exchange.getIn().setHeader(ERROR_MESSAGE_HEADER, "some exception another message");

        try {
            processor.process(exchange);
        } catch (InternalException e) {
            assertEquals(
                    "Internal exception: code='EXTERNAL_SERVICE', message ='some exception another message' cause='null'",
                    e.getMessage());
        }
    }

    @Test
    void getExceptionTest() {
        try {
            throw ErrorGenerateProcessor.getException(ErrorCode.VALIDATION, "some message");
        } catch (InternalException e) {
            assertEquals(
                    "Internal exception: code='VALIDATION', message ='some message'",
                    e.getMessage());
        }
    }
}
