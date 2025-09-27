package org.acme;

import io.quarkus.logging.Log;

public record EchoRequest(String message, String postcode) {
    public EchoRequest {
        Log.info("EchoRequest");
    }
}