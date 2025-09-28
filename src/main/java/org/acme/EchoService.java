package org.acme;

import io.quarkus.logging.Log;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;

@ApplicationScoped
public class EchoService {
    public EchoResponse doWorkValidate(@Valid ValidEchoRequest req) {
        Log.info("valid...");
        return new EchoResponse(req);
    }

    public EchoResponse doWork(ValidEchoRequest req) {
        Log.info("doing work...");
        return new EchoResponse(req);
    }

    public EchoResponse doWork(@Nonnull AlternateValidEchoRequest req) {
        Log.info("doing work alternate...");
        return new EchoResponse(req.message(), req.postcode());
    }
}
