package com.adapter.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Value
public class MessageBRequest {
    @NotNull
    String txt;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    LocalDateTime createdDt;
    @NotNull
    BigInteger currentTemp;

}
