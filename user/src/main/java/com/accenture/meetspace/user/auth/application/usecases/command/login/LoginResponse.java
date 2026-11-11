package com.accenture.meetspace.user.auth.application.usecases.command.login;

import com.accenture.meetspace.user.auth.domain.enums.LoginStatus;

public record LoginResponse(String accessToken, String refreshToken, LoginStatus status, String message) {
}
