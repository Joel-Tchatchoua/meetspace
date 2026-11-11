package com.accenture.meetspace.user.profile.domain.exceptions;

import com.accenture.meetspace.bootstrap.domain.NotFoundException;

public class InitialAdminNotFoundException extends NotFoundException {

    public InitialAdminNotFoundException(String username) {
        super("INITIAL_ADMIN_NOT_FOUND", "No account " + username + " in the identity provider");
    }
}
