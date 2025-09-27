package org.acme;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.logging.Log;

import static org.acme.ValidatorUtil.validateOrThrow;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExampleResource {

    public record EchoRequest(String message) {
        public EchoRequest {
            Log.info("EchoRequest");
        }
    }

    public record ValidEchoRequest(
            @NotBlank
            String message
    ) {
        public ValidEchoRequest {
            Log.info("ValidEchoRequest");
        }
//        public ValidEchoRequest(String message) {
//            Log.info("ValidEchoRequest");
//            this.message = message;
//            ValidatorUtil.validateOrThrow(this);
//        }
    }

    public record EchoResponse(String message, int length) {
        public EchoResponse {
            Log.info("EchoResponse");
        }
    }

    @POST
    @Path("/echo")
    public EchoResponse echo(EchoRequest req) {
        var valid = validateOrThrow(new ValidEchoRequest(req.message()));
        return new EchoResponse(valid.message(), valid.message().length());
    }

    @POST
    @Path("/echo-valid")
    public EchoResponse echoValid(@Valid ValidEchoRequest req) {
        return new EchoResponse(req.message(), req.message().length());
    }
}
