package com.accenture.meetspace.user.shared.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("meetspace.initial-admin")
public record InitialAdminProperties(String username) {
}
