package com.adapter.domain;

import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class Coordinates {
    @NotNull
    String latitude;
    @NotNull
    String longitude;


}
