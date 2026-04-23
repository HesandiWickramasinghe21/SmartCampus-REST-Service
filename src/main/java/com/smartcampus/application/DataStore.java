package com.smartcampus.application;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataStore {

    private static final Map<String, Room>              rooms          = new HashMap<>();
    private static final Map<String, Sensor>            sensors        = new HashMap<>();
    private static final Map<String, List<SensorReading>> sensorReadings = new HashMap<>();

    private static final Object roomsLock    = new Object();
    private static final Object sensorsLock  = new Object();
    private static final Object readingsLock = new Object();

    public static void addRoom(Room room) {
        synchronized (roomsLock) {
            rooms.put(room.getId(), room);
        }
    }

    public static Room getRoom(String roomId) {
        synchronized (roomsLock) {
            return rooms.get(roomId);
        }
    }

    public static List<Room> getAllRooms() {
        synchronized (roomsLock) {
            return new ArrayList<>(rooms.values());
        }
    }

    public static void updateRoom(Room room) {
        synchronized (roomsLock) {
            if (rooms.containsKey(room.getId())) {
                rooms.put(room.getId(), room);
            }
        }
    }

    public static void deleteRoom(String roomId) {
        synchronized (roomsLock) {
            rooms.remove(roomId);
        }
    }

    public static boolean roomExists(String roomId) {
        synchronized (roomsLock) {
            return rooms.containsKey(roomId);
        }
    }

    public static void addSensor(Sensor sensor) {
        synchronized (sensorsLock) {
            sensors.put(sensor.getId(), sensor);
        }
        
        Room room = getRoom(sensor.getRoomId());
        if (room != null) {
            room.addSensorId(sensor.getId());
            updateRoom(room);
        }
    }

    public static Sensor getSensor(String sensorId) {
        synchronized (sensorsLock) {
            return sensors.get(sensorId);
        }
    }

    public static List<Sensor> getAllSensors() {
        synchronized (sensorsLock) {
            return new ArrayList<>(sensors.values());
        }
    }

    public static List<Sensor> getSensorsByType(String type) {
        synchronized (sensorsLock) {
            return sensors.values().stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
    }

    public static List<Sensor> getSensorsByRoom(String roomId) {
        synchronized (sensorsLock) {
            return sensors.values().stream()
                    .filter(s -> roomId.equals(s.getRoomId()))
                    .collect(Collectors.toList());
        }
    }

    public static void updateSensor(Sensor sensor) {
        synchronized (sensorsLock) {
            if (sensors.containsKey(sensor.getId())) {
                sensors.put(sensor.getId(), sensor);
            }
        }
    }

    public static void deleteSensor(String sensorId) {
        String roomId = null;

        synchronized (sensorsLock) {
            Sensor sensor = sensors.remove(sensorId);
            if (sensor != null) {
                roomId = sensor.getRoomId();
            }
        }

        if (roomId != null) {
            Room room = getRoom(roomId);
            if (room != null) {
                room.removeSensorId(sensorId);
                updateRoom(room);
            }
        }
    }

    public static boolean sensorExists(String sensorId) {
        synchronized (sensorsLock) {
            return sensors.containsKey(sensorId);
        }
    }

    public static void addSensorReading(String sensorId, SensorReading reading) {
        synchronized (readingsLock) {
            sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
        }
    }

    public static List<SensorReading> getSensorReadings(String sensorId) {
        synchronized (readingsLock) {
            List<SensorReading> readings = sensorReadings.get(sensorId);
            return readings != null ? new ArrayList<>(readings) : new ArrayList<>();
        }
    }

    public static SensorReading getSensorReading(String sensorId, String readingId) {
        synchronized (readingsLock) {
            List<SensorReading> readings = sensorReadings.get(sensorId);
            if (readings != null) {
                return readings.stream()
                        .filter(r -> r.getId().equals(readingId))
                        .findFirst()
                        .orElse(null);
            }
            return null;
        }
    }

    public static void deleteSensorReadings(String sensorId) {
        synchronized (readingsLock) {
            sensorReadings.remove(sensorId);
        }
    }

    public static void clearAllData() {
        synchronized (roomsLock)    { rooms.clear(); }
        synchronized (sensorsLock)  { sensors.clear(); }
        synchronized (readingsLock) { sensorReadings.clear(); }
    }

    public static void initializeWithSampleData() {
        Room room1 = new Room("LIB-301",  "Library Quiet Study", 50);
        Room room2 = new Room("LAB-401",  "Electronics Laboratory", 30);
        Room room3 = new Room("HALL-001", "Main Hall", 200);

        addRoom(room1);
        addRoom(room2);
        addRoom(room3);

        Sensor temp1      = new Sensor("TEMP-001",  "Temperature", "ACTIVE",      22.5,  "LIB-301");
        Sensor co2_1      = new Sensor("CO2-001",   "CO2",         "ACTIVE",      450.0, "LIB-301");
        Sensor occupancy1 = new Sensor("OCC-001",   "Occupancy",   "ACTIVE",      25.0,  "LAB-401");
        Sensor light1     = new Sensor("LIGHT-001", "Lighting",    "MAINTENANCE", 80.0,  "HALL-001");

        addSensor(temp1);
        addSensor(co2_1);
        addSensor(occupancy1);
        addSensor(light1);
    }
}
