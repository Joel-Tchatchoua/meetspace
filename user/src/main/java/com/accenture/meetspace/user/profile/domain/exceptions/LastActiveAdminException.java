package com.accenture.meetspace.user.profile.domain.exceptions;

import com.accenture.meetspace.bootstrap.domain.BusinessRuleViolationException;

public class LastActiveAdminException extends BusinessRuleViolationException {

    public LastActiveAdminException() {
        super("LAST_ACTIVE_ADMIN", "The last active admin cannot be demoted");
    }
}
