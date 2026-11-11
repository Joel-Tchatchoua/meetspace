package com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.validation;

import com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.requests.RegisterEmployeeRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, RegisterEmployeeRequest> {

    @Override
    public boolean isValid(RegisterEmployeeRequest request, ConstraintValidatorContext context) {
        if (Objects.equals(request.password(), request.passwordConfirmation())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("passwordConfirmation")
                .addConstraintViolation();
        return false;
    }
}
