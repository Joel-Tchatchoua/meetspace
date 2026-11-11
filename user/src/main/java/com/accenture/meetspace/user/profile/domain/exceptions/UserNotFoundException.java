package com.accenture.meetspace.user.profile.domain.exceptions;

import com.accenture.meetspace.bootstrap.domain.NotFoundException;

import java.util.UUID;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(UUID userId) {
        this(userId.toString());
    }

    public UserNotFoundException(String user) {
        super("USER_NOT_FOUND", "User " + user + " not found");
    }
}
