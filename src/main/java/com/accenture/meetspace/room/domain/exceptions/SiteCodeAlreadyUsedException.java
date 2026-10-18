package com.accenture.meetspace.room.domain.exceptions;

import com.accenture.meetspace.shared.domain.BusinessRuleViolationException;

public class SiteCodeAlreadyUsedException extends BusinessRuleViolationException {

    public SiteCodeAlreadyUsedException(String siteCode) {
        super("SITE_CODE_ALREADY_USED", "A site with code " + siteCode + " already exists");
    }
}
