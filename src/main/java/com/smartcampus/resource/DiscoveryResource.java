package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscovery() {
        Map<String, Object> discovery = new LinkedHashMap<>();

        discovery.put("apiVersion", "1.0.0");
        discovery.put("title", "Smart Campus Sensor & Room Management API");
        discovery.put("description", "RESTful API for managing campus rooms and IoT sensor devices");

        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("name", "Smart Campus Team");
        contact.put("email", "admin@smartcampus.edu");
        contact.put("phone", "+1-555-SMART-01");
        discovery.put("contact", contact);

        Map<String, String> links = new LinkedHashMap<>();
        links.put("self",          "/api/v1");
        links.put("rooms",         "/api/v1/rooms");
        links.put("sensors",       "/api/v1/sensors");
        discovery.put("links", links);

        Map<String, String> server = new LinkedHashMap<>();
        server.put("name", "Smart Campus API Server");
        server.put("baseUrl", "/api/v1");
        discovery.put("server", server);

        Map<String, String> ops = new LinkedHashMap<>();
        ops.put("listRooms",           "GET  /api/v1/rooms");
        ops.put("createRoom",          "POST /api/v1/rooms");
        ops.put("getRoom",             "GET  /api/v1/rooms/{roomId}");
        ops.put("deleteRoom",          "DELETE /api/v1/rooms/{roomId}");
        ops.put("listSensors",         "GET  /api/v1/sensors");
        ops.put("listSensorsByType",   "GET  /api/v1/sensors?type={type}");
        ops.put("registerSensor",      "POST /api/v1/sensors");
        ops.put("getSensor",           "GET  /api/v1/sensors/{sensorId}");
        ops.put("listReadings",        "GET  /api/v1/sensors/{sensorId}/readings");
        ops.put("recordReading",       "POST /api/v1/sensors/{sensorId}/readings");
        discovery.put("availableOperations", ops);

        return Response.ok(discovery).build();
    }
}
