package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.ErrorResponse;
import com.smartcampus.model.Sensor;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private static final Logger LOGGER = Logger.getLogger(SensorResource.class.getName());

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        LOGGER.log(Level.INFO, "Retrieving sensors. Filter type: " + (type != null ? type : "none"));

        List<Sensor> sensors;
        if (type != null && !type.isEmpty()) {
            sensors = DataStore.getSensorsByType(type);
        } else {
            sensors = DataStore.getAllSensors();
        }
        LOGGER.log(Level.INFO, "Returning " + sensors.size() + " sensor(s)");
        return Response.ok(sensors).build();
    }

    @POST
    public Response createSensor(Sensor sensor) {
        LOGGER.log(Level.INFO, "Creating new sensor: " + sensor.getId());

        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            ErrorResponse error = new ErrorResponse(400, "Sensor ID cannot be empty");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error).type(MediaType.APPLICATION_JSON).build();
        }

        if (sensor.getRoomId() == null || sensor.getRoomId().isEmpty()) {
            throw new LinkedResourceNotFoundException("Room ID must be specified for sensor registration");
        }

        if (!DataStore.roomExists(sensor.getRoomId())) {
            LOGGER.log(Level.WARNING, "Cannot create sensor: room not found - " + sensor.getRoomId());
            throw new LinkedResourceNotFoundException(
                "Room with ID '" + sensor.getRoomId() + "' does not exist. Cannot assign sensor to non-existent room.");
        }

        if (DataStore.sensorExists(sensor.getId())) {
            ErrorResponse error = new ErrorResponse(409, "Sensor with ID '" + sensor.getId() + "' already exists");
            return Response.status(Response.Status.CONFLICT)
                    .entity(error).type(MediaType.APPLICATION_JSON).build();
        }

        DataStore.addSensor(sensor);
        LOGGER.log(Level.INFO, "Sensor created successfully: " + sensor.getId());
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        LOGGER.log(Level.INFO, "Retrieving sensor: " + sensorId);

        Sensor sensor = DataStore.getSensor(sensorId);
        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor with ID '" + sensorId + "' not found");
        }

        return Response.ok(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadings(@PathParam("sensorId") String sensorId) {
        LOGGER.log(Level.INFO, "Locating readings sub-resource for sensor: " + sensorId);
        return new SensorReadingResource(sensorId);
    }
}
