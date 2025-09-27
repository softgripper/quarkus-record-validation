package org.acme;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import java.util.Comparator;
import java.util.stream.Collectors;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;

public final class ValidatorUtil {
    private static final Validator VALIDATOR = buildDefaultValidatorFactory().getValidator();

    private ValidatorUtil() {
    }

    public static <T> T validateOrThrow(T bean) {
        var violations = VALIDATOR.validate(bean);
        if (!violations.isEmpty()) {
            var msg = violations.stream()
                    .sorted(Comparator.comparing(a -> a.getPropertyPath().toString()))
                    .map(cv -> cv.getPropertyPath() + " " + cv.getMessage())
                    .collect(Collectors.joining(System.lineSeparator()));
            throw new ConstraintViolationException("Validation failed:\n" + msg, violations);
        }
        return bean;
    }
}