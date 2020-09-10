package com.adapter;

import com.adapter.client.MicroserviceBClient;
import com.adapter.client.weather.CommonWeatherClient;
import com.adapter.domain.Coordinates;
import com.adapter.domain.MessageARequest;
import com.adapter.exception.ErrorCode;
import com.adapter.exception.InternalException;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.DefaultMessage;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CamelRouterTest {
    @MockBean
    private CommonWeatherClient commonWeatherClient;
    @MockBean
    private MicroserviceBClient microserviceBClient;
    @Autowired
    private CamelContext camelContext;
    @EndpointInject(uri = "direct:processWeatherGreed")
    private ProducerTemplate producer;

    private Exchange getExchange() {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(new DefaultMessage(camelContext));
        return exchange;
    }

    @Test
    void validationTest() {
        Exchange exchange = getExchange();
        producer.send(exchange);
        Assertions.assertEquals(ErrorCode.VALIDATION, (exchange.getException(InternalException.class)).getErrorCode());

        exchange = getExchange();
        exchange.getIn().setBody(new MessageARequest(null, null, null));
        producer.send(exchange);
        Assertions.assertEquals(ErrorCode.VALIDATION, (exchange.getException(InternalException.class)).getErrorCode());

        exchange = getExchange();
        exchange.getIn().setBody(new MessageARequest("", MessageARequest.Language.ru, new Coordinates(null, null)));
        producer.send(exchange);
        Assertions.assertEquals(ErrorCode.VALIDATION, (exchange.getException(InternalException.class)).getErrorCode());
    }

    @Test
    void ignoreNotRuTest() throws Exception {
        Exchange exchange = getExchange();
        exchange.getIn().setBody(new MessageARequest("asd", MessageARequest.Language.en, new Coordinates("1", "2")));
        producer.send(exchange);
        verify(commonWeatherClient, times(0)).process(any(Exchange.class));
        verify(microserviceBClient, times(0)).process(any(Exchange.class));

        exchange = getExchange();
        exchange.getIn().setBody(new MessageARequest("asd", MessageARequest.Language.es, new Coordinates("1", "2")));
        producer.send(exchange);
        verify(commonWeatherClient, times(0)).process(any(Exchange.class));
        verify(microserviceBClient, times(0)).process(any(Exchange.class));
    }

    @Test
    void notIgnoreRuTest() throws Exception {
        Exchange exchange = getExchange();
        exchange.getIn().setBody(new MessageARequest("asd", MessageARequest.Language.ru, new Coordinates("1", "2")));
        producer.send(exchange);
        verify(commonWeatherClient).process(any(Exchange.class));
        verify(microserviceBClient).process(any(Exchange.class));
    }
}
