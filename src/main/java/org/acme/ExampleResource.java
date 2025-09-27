package org.acme;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;


@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExampleResource {

    public record EchoRequest(String message) {
    }

    public record EchoResponse(String message) {
    }

    @POST
    @Path("/echo")
    public EchoResponse echo(EchoRequest req) {
        return new EchoResponse(req.message());
    }
}
