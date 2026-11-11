package com.accenture.meetspace.user.profile.domain.exceptions;

import com.accenture.meetspace.bootstrap.domain.BusinessRuleViolationException;

public class IdentityAlreadyExistsException extends BusinessRuleViolationException {

    public IdentityAlreadyExistsException(String username) {
        super("IDENTITY_ALREADY_EXISTS", "An account already exists in the identity provider for " + username);
    }
}
