package com.accenture.meetspace.user.profile.domain.exceptions;

import com.accenture.meetspace.bootstrap.domain.DomainException;

public class ExternalServiceUnavailableException extends DomainException {

    public ExternalServiceUnavailableException(String service) {
        super("SERVICE_UNAVAILABLE", service + " is not available");
    }
}
