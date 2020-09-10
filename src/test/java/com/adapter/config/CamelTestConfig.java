package com.adapter.config;

import com.adapter.client.MicroserviceBClient;
import com.adapter.client.weather.CommonWeatherClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

public class CamelTestConfig {

    @Bean
    @Primary
    public CommonWeatherClient commonWeatherClient(RestTemplate restTemplate) {
        return new MockCommonWeatherClient(restTemplate, "1", "2");
    }

    @Bean
    @Primary
    public MicroserviceBClient microserviceBClient(RestTemplate restTemplate) {
        return new MockMicroserviceBClient(restTemplate, "1");
    }

    public static class MockCommonWeatherClient extends CommonWeatherClient {
        public MockCommonWeatherClient(RestTemplate restTemplate, String latitude, String longitude) {
            super(restTemplate, latitude, longitude);
        }
    }

    public static class MockMicroserviceBClient extends MicroserviceBClient {
        public MockMicroserviceBClient(RestTemplate restTemplate, String baseUrl) {
            super(restTemplate, baseUrl);
        }
    }
}