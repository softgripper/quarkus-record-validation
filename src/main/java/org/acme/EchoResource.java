package org.acme;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EchoResource {
    @Inject
    Validator validator;

    @Inject
    EchoService echoService;

    @POST
    @Path("/echo-raw")
    public EchoResponse echoRaw(EchoRequest req) {
        var valid = new ValidEchoRequest(req);
        var violations = validator.validate(valid);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return echoService.doWork(valid);
    }

    @POST
    @Path("/echo-from-service")
    public EchoResponse echoFromService(EchoRequest req) {
        return echoService.doWorkValidate(new ValidEchoRequest(req));
    }

    @POST
    @Path("/echo-valid")
    public EchoResponse echoValid(@Valid ValidEchoRequest req) {
        return echoService.doWork(req);
    }
}
