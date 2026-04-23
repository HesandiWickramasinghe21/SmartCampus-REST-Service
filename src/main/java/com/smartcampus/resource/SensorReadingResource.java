package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private static final Logger LOGGER = Logger.getLogger(SensorReadingResource.class.getName());
    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        LOGGER.log(Level.INFO, "Retrieving all readings for sensor: " + sensorId);

        if (!DataStore.sensorExists(sensorId)) {
            throw new ResourceNotFoundException("Sensor with ID '" + sensorId + "' not found");
        }

        List<SensorReading> readings = DataStore.getSensorReadings(sensorId);
        LOGGER.log(Level.INFO, "Found " + readings.size() + " reading(s) for sensor: " + sensorId);
        return Response.ok(readings).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
        LOGGER.log(Level.INFO, "Adding reading for sensor: " + sensorId);

        Sensor sensor = DataStore.getSensor(sensorId);
        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor with ID '" + sensorId + "' not found");
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            LOGGER.log(Level.WARNING, "Cannot record reading: sensor in MAINTENANCE - " + sensorId);
            throw new SensorUnavailableException(
                "Sensor '" + sensorId + "' is currently under MAINTENANCE and cannot accept new readings");
        }

        if ("OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            LOGGER.log(Level.WARNING, "Cannot record reading: sensor OFFLINE - " + sensorId);
            throw new SensorUnavailableException(
                "Sensor '" + sensorId + "' is currently OFFLINE and cannot accept new readings");
        }

        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        DataStore.addSensorReading(sensorId, reading);

        sensor.setCurrentValue(reading.getValue());
        DataStore.updateSensor(sensor);

        LOGGER.log(Level.INFO,
            "Reading recorded for sensor " + sensorId + " | value=" + reading.getValue());
        return Response.status(Response.Status.CREATED).entity(reading).build();
    }

    @GET
    @Path("/{readingId}")
    public Response getReading(@PathParam("readingId") String readingId) {
        LOGGER.log(Level.INFO, "Retrieving reading " + readingId + " for sensor: " + sensorId);

        SensorReading reading = DataStore.getSensorReading(sensorId, readingId);
        if (reading == null) {
            throw new ResourceNotFoundException(
                "Reading with ID '" + readingId + "' not found for sensor '" + sensorId + "'");
        }

        return Response.ok(reading).build();
    }
}
