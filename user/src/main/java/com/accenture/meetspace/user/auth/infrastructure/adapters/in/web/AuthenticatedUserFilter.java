package com.accenture.meetspace.user.auth.infrastructure.adapters.in.web;

import com.accenture.meetspace.bootstrap.infrastructure.web.security.AuthenticatedUser;
import com.accenture.meetspace.bootstrap.infrastructure.web.security.ProblemDetailsWriter;
import com.accenture.meetspace.user.profile.application.port.out.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthenticatedUserFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final ProblemDetailsWriter problemDetailsWriter;

    public AuthenticatedUserFilter(UserRepository userRepository, ProblemDetailsWriter problemDetailsWriter) {
        this.userRepository = userRepository;
        this.problemDetailsWriter = problemDetailsWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken token) {
            var username = token.getName();
            var user = userRepository.findByUsername(username);
            if (user.isEmpty()) {
                problemDetailsWriter.write(request, response, HttpStatus.FORBIDDEN, "Profile not found",
                        "PROFILE_NOT_FOUND", "No profile exists for " + username);
                return;
            }
            if (!user.get().isActive()) {
                problemDetailsWriter.write(request, response, HttpStatus.FORBIDDEN, "Account disabled",
                        "ACCOUNT_DISABLED", "The account of " + username + " is disabled");
                return;
            }
            request.setAttribute(AuthenticatedUser.ID_ATTRIBUTE, user.get().snapshot().id());
        }
        chain.doFilter(request, response);
    }
}
