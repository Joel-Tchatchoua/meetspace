package com.accenture.meetspace.room.domain.exceptions;

import com.accenture.meetspace.shared.domain.BusinessRuleViolationException;

public class SiteNameAlreadyUsedException extends BusinessRuleViolationException {

    public SiteNameAlreadyUsedException(String siteName) {
        super("SITE_NAME_ALREADY_USED", "A site named " + siteName + " already exists");
    }
}
