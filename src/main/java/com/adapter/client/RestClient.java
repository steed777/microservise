package com.adapter.client;

import com.adapter.exception.InternalException;

public interface RestClient {
    Object callService(Object o) throws InternalException;
}
