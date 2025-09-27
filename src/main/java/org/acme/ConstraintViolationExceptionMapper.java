package org.acme;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(@Nonnull ConstraintViolationException e) {
        var violations = e.getConstraintViolations()
                .stream()
                .map(v -> new Violation(
                        v.getPropertyPath() == null ? "" : v.getPropertyPath().toString(),
                        v.getMessage()
                ))
                .toList();

        var payload = new ViolationReport("Constraint Violation", 400, violations);

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(payload)
                .build();
    }

    public record ViolationReport(String title, int status, List<Violation> violations) {
    }

    public record Violation(String field, String message) {
    }
}