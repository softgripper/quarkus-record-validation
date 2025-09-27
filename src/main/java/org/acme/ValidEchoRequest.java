package org.acme;

import io.quarkus.logging.Log;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import static org.acme.NfcWhitelistSanitizer.sanitize;

public record ValidEchoRequest(
        @NotBlank
        String message,

        @NotEmpty
        @Pattern(regexp = "\\d{4}", message = "must be 4 digits")
        String postcode
) {
    public ValidEchoRequest(String message, String postcode) {
        Log.info("ValidEchoRequest");
        this.message = sanitize("message", message, true);
        this.postcode = sanitize("postcode", postcode);
    }

    public ValidEchoRequest(@Nonnull EchoRequest req) {
        this(req.message(), req.postcode());
    }
}