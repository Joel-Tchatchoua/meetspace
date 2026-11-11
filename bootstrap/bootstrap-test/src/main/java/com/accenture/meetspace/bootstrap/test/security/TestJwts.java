package com.accenture.meetspace.bootstrap.test.security;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestJwts {

    private final JwtEncoder encoder;

    public TestJwts(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    public String employee(String username) {
        return token(username, "EMPLOYEE", Instant.now().plus(Duration.ofHours(1)));
    }

    public String admin(String username) {
        return token(username, "ADMIN", Instant.now().plus(Duration.ofHours(1)));
    }

    public String service(String username) {
        return token(username, "SERVICE", Instant.now().plus(Duration.ofHours(1)));
    }

    public String expired(String username) {
        return token(username, "EMPLOYEE", Instant.now().minus(Duration.ofHours(1)));
    }

    private String token(String username, String role, Instant expiresAt) {
        var claims = JwtClaimsSet.builder()
                .subject(UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8)).toString())
                .issuedAt(expiresAt.minus(Duration.ofHours(2)))
                .expiresAt(expiresAt)
                .claim("preferred_username", username)
                .claim("email", username + "@accenture.com")
                .claim("given_name", "Test")
                .claim("family_name", username)
                .claim("realm_access", Map.of("roles", List.of(role)))
                .build();
        var header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
