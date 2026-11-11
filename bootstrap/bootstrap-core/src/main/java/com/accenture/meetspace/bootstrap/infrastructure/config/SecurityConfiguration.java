package com.accenture.meetspace.bootstrap.infrastructure.config;

import com.accenture.meetspace.bootstrap.infrastructure.web.security.ProblemDetailsWriter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class SecurityConfiguration {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String USERNAME_CLAIM = "preferred_username";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectProvider<JwtDecoder> jwtDecoder,
                                            ProblemDetailsWriter problemDetailsWriter) {
        var invalidToken = invalidToken(problemDetailsWriter);
        jwtDecoder.ifAvailable(decoder -> http.oauth2ResourceServer(server -> server
                .authenticationEntryPoint(invalidToken)
                .jwt(jwt -> jwt.decoder(decoder).jwtAuthenticationConverter(jwtAuthenticationConverter()))));
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(invalidToken))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/error", "/actuator/health", "/actuator/health/**")
                        .permitAll()
                        .requestMatchers("/api/*/auth/register", "/api/*/auth/login", "/api/*/auth/logout")
                        .permitAll()
                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                        .permitAll()
                        .anyRequest().authenticated())
                .build();
    }

    private static AuthenticationEntryPoint invalidToken(ProblemDetailsWriter problemDetailsWriter) {
        return (request, response, exception) -> problemDetailsWriter.write(request, response,
                HttpStatus.UNAUTHORIZED, "Authentication required", "UNAUTHENTICATED",
                "A valid token is required");
    }

    private static JwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName(USERNAME_CLAIM);
        converter.setJwtGrantedAuthoritiesConverter(realmRoles());
        return converter;
    }

    private static Converter<Jwt, Collection<GrantedAuthority>> realmRoles() {
        return jwt -> {
            var realmAccess = jwt.getClaimAsMap(REALM_ACCESS_CLAIM);
            if (realmAccess == null || !(realmAccess.get(ROLES_CLAIM) instanceof Collection<?> roles)) {
                return List.of();
            }
            return roles.stream()
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
        };
    }
}
