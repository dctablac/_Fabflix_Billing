package edu.uci.ics.dtablac.service.billing.resources;

import edu.uci.ics.dtablac.service.billing.logger.ServiceLogger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("test")
public class TestPage {
    @Path("hello")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response test() {
        ServiceLogger.LOGGER.info("Hello there!");
        System.out.println("Hello");
        return Response.status(Status.OK).build();
    }
}