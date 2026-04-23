package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    
    private static final Logger LOGGER = Logger.getLogger(ApiLoggingFilter.class.getName());
    private static final String REQUEST_TIME_PROPERTY = "request.start.time";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        requestContext.setProperty(REQUEST_TIME_PROPERTY, System.currentTimeMillis());
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();
        
        LOGGER.log(Level.INFO, String.format("[API REQUEST] Method: %s | URI: %s", method, uri));
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        Long startTime = (Long) requestContext.getProperty(REQUEST_TIME_PROPERTY);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
        int statusCode = responseContext.getStatus();
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();
        
        LOGGER.log(Level.INFO, String.format(
            "[API RESPONSE] Method: %s | URI: %s | Status: %d | Duration: %dms", 
            method, uri, statusCode, duration
        ));
    }
}
