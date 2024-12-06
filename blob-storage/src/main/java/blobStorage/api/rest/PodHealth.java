package blobStorage.api.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/health")
public class PodHealth {

    @GET
    public Response getHealthPing() {
        return Response.ok().build();
    }
}
