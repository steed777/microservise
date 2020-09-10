package com.adapter.client;

import com.adapter.domain.Coordinates;
import com.adapter.domain.MessageARequest;
import com.adapter.exception.InternalException;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.DefaultMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.test.web.client.match.ContentRequestMatchers;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.util.regex.Pattern;

import static com.adapter.contant.ExchangeHeaders.WEATHER_TEMP_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@ActiveProfiles("test")
class MicroserviceBClientTest {
    private MicroserviceBClient microserviceBClient;

    @Value("${application.destination-service-properties.base-url}")
    private String baseUrl;

    private static RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        class CustomContentRequestMatchers extends ContentRequestMatchers {
            @Override
            public RequestMatcher string(String expectedContent) {
                return request -> {
                    MockClientHttpRequest mockRequest = (MockClientHttpRequest) request;
                    assertTrue(Pattern.matches(expectedContent, mockRequest.getBodyAsString()));
                };
            }
        }
        MockRestServiceServer.bindTo(restTemplate)
                .build()
                .expect(manyTimes(), requestTo("https://test/service/b"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(new CustomContentRequestMatchers().string("\\{\"txt\":\"asdasd\",\"createdDt\":\"[\\dTZ:-]*\",\"currentTemp\":12}"))
                .andRespond(withSuccess("{\"getSomeInfo\":true}", MediaType.APPLICATION_JSON));

        return restTemplate;
    }


    @BeforeEach
    public void SetUp() {
        microserviceBClient = new MicroserviceBClient(getRestTemplate(), baseUrl);
    }

    @Test
    void callServiceTest() throws InternalException {
        assertEquals("{\"getSomeInfo\":true}", microserviceBClient.callService("{\"txt\":\"asdasd\",\"createdDt\":\"2020-09-03T18:43:56Z\",\"currentTemp\":12}"));
    }

    @Test
    void callServiceExceptionTest() {
        try {
            microserviceBClient = new MicroserviceBClient(new RestTemplate(), "invalid");
            microserviceBClient.callService("");
        } catch (InternalException e) {
            assertEquals("Internal exception: code='EXTERNAL_SERVICE', " +
                    "message ='problem during call weather service: URI is not absolute' " +
                    "cause='URI is not absolute'", e.getMessage());
        }
    }

    @Test
    void processTest() throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(new DefaultMessage(camelContext));
        exchange.getIn().setHeader(WEATHER_TEMP_HEADER, BigInteger.valueOf(12));
        exchange.getIn().setBody(new MessageARequest("asdasd", MessageARequest.Language.ru, new Coordinates("123", "321")));
        microserviceBClient.process(exchange);
    }

    @Test
    void processExceptionTest() throws Exception {
        microserviceBClient = new MicroserviceBClient(new RestTemplate(), "invalid");
        CamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(new DefaultMessage(camelContext));
        exchange.getIn().setHeader(WEATHER_TEMP_HEADER, BigInteger.valueOf(12));
        exchange.getIn().setBody(new MessageARequest("asdasd", MessageARequest.Language.ru, new Coordinates("123", "321")));
        try {
            microserviceBClient.process(exchange);
        } catch (InternalException e) {
            assertEquals("Internal exception: code='EXTERNAL_SERVICE', " +
                    "message ='problem during call weather service: URI is not absolute' " +
                    "cause='URI is not absolute'", e.getMessage());
        }
    }
}
