package org.acme.validation;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

public final class ValidatorUtil {
    private ValidatorUtil() {
    }

    private static volatile Validator internalValidator;

    private static Validator resolveValidator() {
        if (internalValidator != null) {
            return internalValidator;
        }
        try {
            return internalValidator = CDI.current().select(Validator.class).get();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "CDI is not initialized or the Validator bean is not available. " +
                            "Ensure this is called after the container has started and that validation is enabled.",
                    e
            );
        }
    }

    public static <T> void validate(T bean) {
        var v = resolveValidator();
        var violations = v.validate(bean);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
