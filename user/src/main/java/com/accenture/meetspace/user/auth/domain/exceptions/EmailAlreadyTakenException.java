package com.accenture.meetspace.user.auth.domain.exceptions;

import com.accenture.meetspace.bootstrap.domain.BusinessRuleViolationException;

public class EmailAlreadyTakenException extends BusinessRuleViolationException {

    public EmailAlreadyTakenException(String email) {
        super("EMAIL_ALREADY_TAKEN", "Email " + email + " is already taken");
    }
}
