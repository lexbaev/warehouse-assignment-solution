package com.fulfilment.application.monolith.exceptions;

public class BusinessRuleViolationException extends Exception {
    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
