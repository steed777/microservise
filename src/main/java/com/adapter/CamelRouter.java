package com.adapter;

import com.adapter.contant.ExchangeHeaders;
import com.adapter.domain.MessageARequest;
import com.adapter.exception.ErrorCode;
import com.adapter.exception.ErrorGenerateProcessor;
import lombok.Setter;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CamelRouter extends RouteBuilder {
    private static final String VALIDATION_ROUTE = "direct:validation";

    @Setter
    @Value("${application.service-id}")
    private String serviceId;

    @Override
    public void configure() {

        rest("/request").description("Weather data greed")
                .consumes("application/json; charset=utf-8")
                .produces("application/json; charset=utf-8")

                .post().description("Do weather things").type(MessageARequest.class)
                .responseMessage().code(200).message("all weather data has been successfully transmitted").endResponseMessage()
                .to("direct:processWeatherGreed");

        from("direct:processWeatherGreed")
                .to(VALIDATION_ROUTE)
                .filter(exchange -> exchange.getIn().getBody(MessageARequest.class).getLng() == MessageARequest.Language.ru)
                .setHeader(ExchangeHeaders.WEATHER_SERVICE_ID_HEADER, () -> serviceId)
                .process("commonWeatherClient")
                .process("microserviceBClient");

        from(VALIDATION_ROUTE)
                .doTry()
                    .to("bean-validator:beanValidation")
                .doCatch(Exception.class)
                    .setHeader(ErrorGenerateProcessor.ERROR_CODE_HEADER, () -> ErrorCode.VALIDATION)
                    .setHeader(ErrorGenerateProcessor.ERROR_MESSAGE_HEADER, () -> "validation error")
                    .process("errorGenerateProcessor")
                .end();
    }
}