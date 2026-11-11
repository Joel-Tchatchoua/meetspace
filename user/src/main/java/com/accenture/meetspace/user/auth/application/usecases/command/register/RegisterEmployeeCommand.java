package com.accenture.meetspace.user.auth.application.usecases.command.register;

import com.accenture.meetspace.bootstrap.application.command.Command;
import com.accenture.meetspace.bootstrap.application.command.InvalidCommandException;

import java.util.UUID;

import static com.accenture.meetspace.bootstrap.application.command.CommandPreconditions.requireNonNull;
import static com.accenture.meetspace.bootstrap.application.command.CommandPreconditions.requireNotBlank;

public record RegisterEmployeeCommand(UUID userId, String username, String email, String password, String firstName,
                                      String lastName) implements Command<RegisterEmployeeResponse> {

    private static final int PASSWORD_MIN_LENGTH = 8;

    public RegisterEmployeeCommand {
        requireNonNull(userId, "userId");
        requireNotBlank(username, "username");
        requireNotBlank(email, "email");
        requireStrong(password);
        requireNotBlank(firstName, "firstName");
        requireNotBlank(lastName, "lastName");
    }

    private static void requireStrong(String password) {
        if (password == null
                || password.length() < PASSWORD_MIN_LENGTH
                || password.chars().noneMatch(Character::isUpperCase)
                || password.chars().noneMatch(Character::isDigit)
                || password.chars().noneMatch(RegisterEmployeeCommand::isSymbol)) {
            throw new InvalidCommandException("password",
                    "must have at least 8 characters, including an upper-case letter, a digit and a symbol");
        }
    }

    private static boolean isSymbol(int character) {
        return !Character.isLetterOrDigit(character) && !Character.isWhitespace(character);
    }

    @Override
    public String toString() {
        return "RegisterEmployeeCommand[userId=" + userId + ", username=" + username + "]";
    }
}
