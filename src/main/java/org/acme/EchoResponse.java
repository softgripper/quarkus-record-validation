package org.acme;

import io.quarkus.logging.Log;
import jakarta.annotation.Nonnull;

public record EchoResponse(String message, String postcode) {
    public EchoResponse {
        Log.info("EchoResponse");
    }

    public EchoResponse(@Nonnull ValidEchoRequest req) {
        this(req.message(), req.postcode());
    }
}