package com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.requests;

import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommand;
import com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.validation.PasswordsMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@PasswordsMatch
public record RegisterEmployeeRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(max = 255) String email,
        @NotBlank String password,
        @NotBlank String passwordConfirmation,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName) {

    public RegisterEmployeeCommand toCommand() {
        return new RegisterEmployeeCommand(UUID.randomUUID(), username, email, password, firstName, lastName);
    }

    @Override
    public String toString() {
        return "RegisterEmployeeRequest[username=" + username + "]";
    }
}
