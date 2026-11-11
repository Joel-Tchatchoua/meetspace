package com.accenture.meetspace.user.auth.domain.exceptions;

import com.accenture.meetspace.bootstrap.domain.DomainException;

public class AccountDisabledException extends DomainException {

    public AccountDisabledException(String username) {
        super("ACCOUNT_DISABLED", "The account of " + username + " is disabled");
    }
}
