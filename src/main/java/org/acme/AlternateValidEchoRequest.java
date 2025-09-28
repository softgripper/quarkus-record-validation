package org.acme;

import io.quarkus.logging.Log;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.acme.validation.ValidatorUtil;

import static org.acme.validation.NfcWhitelistSanitizer.sanitize;

public record AlternateValidEchoRequest(
        @NotBlank
        String message,

        @NotEmpty
        @Pattern(regexp = "\\d{4}", message = "must be 4 digits")
        String postcode
) {
    public AlternateValidEchoRequest(String message, String postcode) {
        Log.info("ValidEchoRequest");
        this.message = sanitize("message", message, true);
        this.postcode = sanitize("postcode", postcode);
        ValidatorUtil.validate(this);
    }

    public AlternateValidEchoRequest(@Nonnull EchoRequest req) {
        this(req.message(), req.postcode());
    }
}