package com.accenture.meetspace.user.auth.domain.exceptions;

import com.accenture.meetspace.bootstrap.domain.BusinessRuleViolationException;

public class UsernameAlreadyTakenException extends BusinessRuleViolationException {

    public UsernameAlreadyTakenException(String username) {
        super("USERNAME_ALREADY_TAKEN", "Username " + username + " is already taken");
    }
}
