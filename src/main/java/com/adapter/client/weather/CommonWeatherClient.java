package com.adapter.client.weather;

import com.adapter.domain.MessageARequest;
import com.adapter.exception.ErrorGenerateProcessor;
import com.adapter.exception.InternalException;
import com.adapter.utils.Converter;
import com.google.gson.Gson;
import lombok.Setter;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.adapter.contant.ExchangeHeaders.WEATHER_SERVICE_ID_HEADER;
import static com.adapter.contant.ExchangeHeaders.WEATHER_TEMP_HEADER;
import static com.adapter.exception.ErrorCode.EXTERNAL_SERVICE;

@Configuration
@ConfigurationProperties("application.weather-properties")
public class CommonWeatherClient implements Processor, WeatherRestClient {
    private final String latitude;
    private final String longitude;
    private final RestTemplate restTemplate;
    private final Gson gson = new Gson();

    @Setter
    private Map<String, Map<String, String>> urlMap = new HashMap<>();

    public CommonWeatherClient(@Qualifier("commonWeatherRestTemplate") RestTemplate restTemplate,
                               @Value("${application.weather-properties.latitude-replaceable}") String latitude,
                               @Value("${application.weather-properties.longitude-replaceable}") String longitude) {
        this.restTemplate = restTemplate;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        MessageARequest requestA = exchange.getIn().getBody(MessageARequest.class);
        BigInteger temp = getTemp(
                exchange.getIn().getHeader(WEATHER_SERVICE_ID_HEADER, String.class),
                requestA.getCoordinates().getLatitude(),
                requestA.getCoordinates().getLongitude());
        exchange.getIn().setHeader(WEATHER_TEMP_HEADER, temp);
    }

    @Override
    public BigInteger getTemp(String serviceId, String latitude, String longitude) throws InternalException {
        Map<String, String> serviceProperties = Optional.ofNullable(urlMap.get(serviceId))
                .orElseThrow(() -> ErrorGenerateProcessor.getException(EXTERNAL_SERVICE, "no such service: " + serviceId));
        String baseUrl = Optional.ofNullable(serviceProperties.get("base-url"))
                .orElseThrow(() -> ErrorGenerateProcessor.getException(EXTERNAL_SERVICE, "can't find base url for: " + serviceId))
                .replace(this.latitude, latitude)
                .replace(this.longitude, longitude);
        String pathToTemp = Optional.ofNullable(serviceProperties.get("temp-path"))
                .orElseThrow(() -> ErrorGenerateProcessor.getException(EXTERNAL_SERVICE, "can't find path to temp url for: " + serviceId));
        String weatherResponse = callService(baseUrl);
        String temp = extractTemp(weatherResponse, pathToTemp);
        return Converter.fahrenheitToCelsius(temp);
    }

    private String callService(String url) throws InternalException {
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            throw ErrorGenerateProcessor.getException(EXTERNAL_SERVICE, "problem during call weather service: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractTemp(String body, String path) throws InternalException {
        Map<String, Object> bodyMap = gson.fromJson(body, Map.class);
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        Object o = bodyMap;
        for (String currentPath : path.split("/")) {
            o = getFromCurrentPath(o, currentPath, path);
            if (o == null) {
                throw ErrorGenerateProcessor.getException(EXTERNAL_SERVICE, "can't find path: " + path);
            }
        }

        if (o instanceof Map || o instanceof Collection) {
            throw ErrorGenerateProcessor.getException(EXTERNAL_SERVICE, "result by path '" + path + "' is not simple type");
        } else {
            return String.valueOf(o);
        }
    }

    private Object getFromCurrentPath(Object o, String oneWayPath, String fullPath) throws InternalException {
        if (o instanceof Map) {
            return ((Map<?, ?>) o).get(oneWayPath);
        } else {
            throw ErrorGenerateProcessor.getException(EXTERNAL_SERVICE, "the path is not the only possible one: " + fullPath);
        }
    }
}
