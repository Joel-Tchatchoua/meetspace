package com.accenture.meetspace.shared.domain;

public abstract class DomainValidationException extends DomainException {

    protected DomainValidationException(String code, String message) {
        super(code, message);
    }
}
