package com.adapter.client.weather;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static com.adapter.contant.ExchangeHeaders.WEATHER_SERVICE_ID_HEADER;
import static com.adapter.contant.ExchangeHeaders.WEATHER_TEMP_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@ActiveProfiles("test")
class CommonWeatherClientTest {
    private CommonWeatherClient commonWeatherClient;

    @Value("${application.weather-properties.latitude-replaceable}")
    private String latitude;
    @Value("${application.weather-properties.longitude-replaceable}")
    private String longitude;

    private static RestTemplate getRestTemplate(String expectedUri, String response) {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer.bindTo(restTemplate)
                .build()
                .expect(manyTimes(), requestTo(expectedUri))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        return restTemplate;
    }

    private static Map<String, Map<String, String>> getUrlMap(String url, String pathToTemp) {
        Map<String, Map<String, String>> urlMap = new HashMap<>();
        Map<String, String> properties = new HashMap<>();
        properties.put("base-url", url);
        properties.put("temp-path", pathToTemp);
        urlMap.put("testWeatherService", properties);
        return urlMap;
    }


    @BeforeEach
    public void SetUp() {
        commonWeatherClient = new CommonWeatherClient(getRestTemplate(
                "https://test/service/weather?lat=123&lon=321",
                "{\"some\":{\"where\":281.52}}"),
                latitude, longitude);
        commonWeatherClient.setUrlMap(getUrlMap(
                "https://test/service/weather?lat={latitude}&lon={longitude}",
                "/some/where"));
    }

    @Test
    void getTempTest() throws InternalException {
        assertEquals(BigInteger.valueOf(8), commonWeatherClient.getTemp("testWeatherService", "123", "321"));
    }

    @Test
    void processTest() throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(new DefaultMessage(camelContext));
        exchange.getIn().setBody(new MessageARequest("txt", MessageARequest.Language.ru, new Coordinates("123", "321")));
        exchange.getIn().setHeader(WEATHER_SERVICE_ID_HEADER, "testWeatherService");
        commonWeatherClient.process(exchange);
        assertEquals(BigInteger.valueOf(8), exchange.getIn().getHeader(WEATHER_TEMP_HEADER, BigInteger.class));
    }

    @Test
    void getTempException1Test() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForObject(any(String.class), any(Class.class), any(Object[].class)))
                .thenThrow(new RestClientException("External exception"));
        commonWeatherClient = new CommonWeatherClient(restTemplate, latitude, longitude);
        commonWeatherClient.setUrlMap(getUrlMap("0", "asd"));
        assertException("testWeatherService", "2", "3",
                "Internal exception: code='EXTERNAL_SERVICE', message ='problem during call weather service:" +
                        " External exception' cause='External exception'");
    }

    @Test
    void getTempException2Test() {
        commonWeatherClient = new CommonWeatherClient(getRestTemplate(
                "https://test/service/weather?lat=2&lon=3",
                "{\"some\":{\"where\":{\"where\": 281.52}}}"), latitude, longitude);
        commonWeatherClient.setUrlMap(getUrlMap(
                "https://test/service/weather?lat={latitude}&lon={longitude}",
                "/some/where"));
        assertException("testWeatherService", "2", "3",
                "Internal exception: code='EXTERNAL_SERVICE', message ='result by path 'some/where' is not simple type'");
    }

    @Test
    void getTempException3Test() {
        commonWeatherClient = new CommonWeatherClient(getRestTemplate(
                "https://test/service/weather?lat=2&lon=3",
                "{\"some\":{\"where\":{\"where\": 281.52}}}"), latitude, longitude);
        commonWeatherClient.setUrlMap(getUrlMap(
                "https://test/service/weather?lat={latitude}&lon={longitude}",
                "/some/whereElse"));
        assertException("testWeatherService", "2", "3",
                "Internal exception: code='EXTERNAL_SERVICE', message ='can't find path: some/whereElse'");
    }

    @Test
    void getTempException4Test() {
        commonWeatherClient = new CommonWeatherClient(getRestTemplate(
                "https://test/service/weather?lat=2&lon=3",
                "{\"some\":[{\"where\": 281.52}]}"), latitude, longitude);
        commonWeatherClient.setUrlMap(getUrlMap(
                "https://test/service/weather?lat={latitude}&lon={longitude}",
                "/some/where"));
        assertException("testWeatherService", "2", "3",
                "Internal exception: code='EXTERNAL_SERVICE', message ='the path is not the only possible one: some/where'");
    }

    @Test
    void getTempException5Test() {
        commonWeatherClient = new CommonWeatherClient(getRestTemplate(
                "https://test/service/weather?lat=2&lon=3",
                "{\"some\":[{\"where\": 281.52}]}"), latitude, longitude);
        commonWeatherClient.setUrlMap(getUrlMap(
                "https://test/service/weather?lat={latitude}&lon={longitude}",
                "/some/where"));
        assertException("notTestWeatherService", "2", "3",
                "Internal exception: code='EXTERNAL_SERVICE', message ='no such service: notTestWeatherService'");
    }

    void assertException(String serviceId, String latitude, String longitude, String message) {
        try {
            commonWeatherClient.getTemp(serviceId, latitude, longitude);
            fail();
        } catch (InternalException e) {
            assertEquals(message, e.getMessage());
        }
    }
}
