package com.adapter.client.weather;

import com.adapter.exception.InternalException;

import java.math.BigInteger;

public interface WeatherRestClient {
    BigInteger getTemp(String serviceId, String latitude, String longitude) throws InternalException;
}
