package com.crowdguard.service;

import java.util.Map;

public class RegistrationValidationException extends RuntimeException {
    private final Map<String, String> errors;

    public RegistrationValidationException(Map<String, String> errors) {
        super("Please correct the registration details.");
        this.errors = Map.copyOf(errors);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
