package com.adapter.client;

import com.adapter.domain.MessageARequest;
import com.adapter.domain.MessageBRequest;
import com.adapter.exception.ErrorGenerateProcessor;
import com.adapter.exception.InternalException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.time.LocalDateTime;

import static com.adapter.contant.ExchangeHeaders.WEATHER_TEMP_HEADER;
import static com.adapter.exception.ErrorCode.EXTERNAL_SERVICE;

@Component
public class MicroserviceBClient implements Processor, RestClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public MicroserviceBClient(@Qualifier("microserviceBRestTemplate") RestTemplate restTemplate,
                               @Value("${application.destination-service-properties.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        BigInteger temp = exchange.getIn().getHeader(WEATHER_TEMP_HEADER, BigInteger.class);
        MessageARequest requestA = exchange.getIn().getBody(MessageARequest.class);
        MessageBRequest requestB = new MessageBRequest(
                requestA.getMsg(),
                LocalDateTime.now(),
                temp);
        callService(requestB);
    }

    @Override
    public Object callService(Object o) throws InternalException {
        try {
            return restTemplate.postForObject(baseUrl, o, String.class);
        } catch (Exception e) {
            throw ErrorGenerateProcessor.getException(EXTERNAL_SERVICE, "problem during call weather service: " + e.getMessage(), e);
        }
    }
}
