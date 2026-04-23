package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.ErrorResponse;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private static final Logger LOGGER = Logger.getLogger(RoomResource.class.getName());

    @GET
    public Response getAllRooms() {
        LOGGER.log(Level.INFO, "Retrieving all rooms");
        List<Room> rooms = DataStore.getAllRooms();
        return Response.ok(rooms).build();
    }

    @POST
    public Response createRoom(Room room) {
        LOGGER.log(Level.INFO, "Creating new room: " + room.getId());

        if (room.getId() == null || room.getId().isEmpty()) {
            ErrorResponse error = new ErrorResponse(400, "Room ID cannot be empty");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error).type(MediaType.APPLICATION_JSON).build();
        }

        if (DataStore.roomExists(room.getId())) {
            ErrorResponse error = new ErrorResponse(409, "Room with ID '" + room.getId() + "' already exists");
            return Response.status(Response.Status.CONFLICT)
                    .entity(error).type(MediaType.APPLICATION_JSON).build();
        }

        DataStore.addRoom(room);
        LOGGER.log(Level.INFO, "Room created successfully: " + room.getId());
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        LOGGER.log(Level.INFO, "Retrieving room: " + roomId);

        Room room = DataStore.getRoom(roomId);
        if (room == null) {
            throw new ResourceNotFoundException("Room with ID '" + roomId + "' not found");
        }

        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        LOGGER.log(Level.INFO, "Attempting to delete room: " + roomId);

        Room room = DataStore.getRoom(roomId);
        if (room == null) {
            throw new ResourceNotFoundException("Room with ID '" + roomId + "' not found");
        }

        List<Sensor> activeSensors = DataStore.getSensorsByRoom(roomId);
        if (!activeSensors.isEmpty()) {
            LOGGER.log(Level.WARNING,
                "Cannot delete room " + roomId + ": contains " + activeSensors.size() + " sensor(s)");
            throw new RoomNotEmptyException(
                "Room '" + roomId + "' still contains " + activeSensors.size() + " active sensor(s)");
        }

        DataStore.deleteRoom(roomId);
        LOGGER.log(Level.INFO, "Room deleted successfully: " + roomId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
