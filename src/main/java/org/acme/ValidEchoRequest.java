package org.acme;

import io.quarkus.logging.Log;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record ValidEchoRequest(
        @NotBlank
        String message,

        @NotEmpty
        @Pattern(regexp = "\\d{4}", message = "must be 4 digits")
        String postcode
) {
    public ValidEchoRequest {
        Log.info("ValidEchoRequest");
    }

    public ValidEchoRequest(@Nonnull EchoRequest req) {
        this(req.message(), req.postcode());
    }
}