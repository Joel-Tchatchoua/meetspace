package com.accenture.meetspace.user.auth.domain.exceptions;

import com.accenture.meetspace.bootstrap.domain.DomainException;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", "Invalid username or password");
    }
}
