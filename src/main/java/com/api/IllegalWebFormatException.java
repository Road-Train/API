package com.api;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
public class IllegalWebFormatException extends WebApplicationException
{
    public IllegalWebFormatException(String message)
    {
        super(Response.status(Response.Status.NOT_ACCEPTABLE).entity(message).build());
    }
}
