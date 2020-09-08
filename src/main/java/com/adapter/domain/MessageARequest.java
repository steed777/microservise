package com.adapter.domain;

import lombok.Getter;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Value
public class MessageARequest {
    @NotEmpty
    String msg;
    @NotNull
    Language lng;
    @Valid
    @NotNull
    Coordinates coordinates;

    public enum Language {
        ru, en, es
    }


}
