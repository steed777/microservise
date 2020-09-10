package com.adapter.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConverterTest {
    @Test
    void fahrenheitToCelsius1Test() {
        assertEquals(BigInteger.valueOf(-27), Converter.fahrenheitToCelsius("245.45"));
        assertEquals(BigInteger.valueOf(272), Converter.fahrenheitToCelsius("545.78"));
    }

    @Test
    void fahrenheitToCelsius2Test() {
        assertEquals(BigInteger.valueOf(-27), Converter.fahrenheitToCelsius(new BigDecimal("245.45")));
        assertEquals(BigInteger.valueOf(272), Converter.fahrenheitToCelsius(new BigDecimal("545.78")));
    }
}
