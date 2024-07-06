package com.example.exception.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceNotFoundExceptionWithI18n extends RuntimeException {
    private final String message;
    private final String[] args;

    public ResourceNotFoundExceptionWithI18n(String message, String... args) {
        this.message = message;
        this.args = args;
    }
}