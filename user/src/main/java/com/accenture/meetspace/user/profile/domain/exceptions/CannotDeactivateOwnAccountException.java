package com.accenture.meetspace.user.profile.domain.exceptions;

import com.accenture.meetspace.bootstrap.domain.BusinessRuleViolationException;

public class CannotDeactivateOwnAccountException extends BusinessRuleViolationException {

    public CannotDeactivateOwnAccountException() {
        super("CANNOT_DEACTIVATE_OWN_ACCOUNT", "An admin cannot deactivate their own account");
    }
}
