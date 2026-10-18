package com.accenture.meetspace.shared.domain;

public abstract class BusinessRuleViolationException extends DomainException {

    protected BusinessRuleViolationException(String code, String message) {
        super(code, message);
    }
}
