package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {

        if (exception instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) exception;
            Response original = wae.getResponse();
            ErrorResponse error = new ErrorResponse(
                original.getStatus(),
                wae.getMessage() != null ? wae.getMessage() : Response.Status.fromStatusCode(original.getStatus()).getReasonPhrase()
            );
            return Response.status(original.getStatus())
                    .entity(error)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        LOGGER.log(Level.SEVERE, "Unexpected internal error", exception);

        ErrorResponse error = new ErrorResponse(
            500,
            "Internal Server Error: An unexpected error occurred. Please contact the system administrator."
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
