# Smart Campus API

---

## Overview

This is a RESTful API built using JAX-RS (Jersey) for the Smart Campus coursework. It allows you to manage Rooms and Sensors across a university campus. Sensor readings can also be recorded and retrieved. All data is stored in memory using HashMaps — no database is used.

**Module:** 5COSC022W – Client-Server Architectures
**University of Westminster, 2025/26**

---

## Technologies Used

- Java 11
- JAX-RS / Jersey 2.39.1
- Grizzly HTTP Server
- Jackson (JSON)
- Maven
- NetBeans 24

---

## How to Run

### Requirements
- Java JDK 11+
- Maven 3.6+
- NetBeans IDE

### Steps
1. Open NetBeans and go to **File → Open Project**
2. Select the `smart-campus-api` folder (the one with `pom.xml` inside)
3. Right-click the project and click **Run** (or press F6)
4. You should see something like this in the output:

```
Discovery : http://localhost:57171/api/v1
Rooms     : http://localhost:57171/api/v1/rooms
Sensors   : http://localhost:57171/api/v1/sensors
```

You can also run it from the terminal:
```bash
mvn clean compile exec:java
```

> If port 8080 is busy, the server will pick the next available port automatically and print it in the console.

---

## Base URL

```
http://localhost:57171/api/v1
```

---

## Endpoints

| Endpoint | Method | Description |
|---|---|---|
| `/api/v1` | GET | Returns API info and links (discovery) |
| `/api/v1/rooms` | GET | Get all rooms |
| `/api/v1/rooms` | POST | Create a new room |
| `/api/v1/rooms/{roomId}` | GET | Get a specific room |
| `/api/v1/rooms/{roomId}` | DELETE | Delete a room (fails with 409 if sensors are still in it) |
| `/api/v1/sensors` | GET | Get all sensors, optional `?type=` filter |
| `/api/v1/sensors` | POST | Add a new sensor (roomId must exist, else 422) |
| `/api/v1/sensors/{sensorId}` | GET | Get a specific sensor |
| `/api/v1/sensors/{sensorId}/readings` | GET | Get all readings for a sensor |
| `/api/v1/sensors/{sensorId}/readings` | POST | Add a reading (403 if sensor is in MAINTENANCE) |

---

## Sample curl Commands

```bash
# 1. Discovery endpoint
curl -s http://localhost:57171/api/v1

# 2. List all rooms
curl -s http://localhost:57171/api/v1/rooms

# 3. Get a specific room
curl -s http://localhost:57171/api/v1/rooms/LIB-301

# 4. Create a new room
curl -s -X POST http://localhost:57171/api/v1/rooms ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"CONF-202\",\"name\":\"Conference Room B\",\"capacity\":20}"

# 5. Filter sensors by type
curl -s "http://localhost:57171/api/v1/sensors?type=CO2"

# 6. Register a new sensor
curl -s -X POST http://localhost:57171/api/v1/sensors ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"TEMP-099\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":20.0,\"roomId\":\"LIB-301\"}"

# 7. Add a reading for a sensor
curl -s -X POST http://localhost:57171/api/v1/sensors/TEMP-001/readings ^
  -H "Content-Type: application/json" ^
  -d "{\"value\":24.5}"

# 8. Get all readings for a sensor
curl -s http://localhost:57171/api/v1/sensors/TEMP-001/readings

# 9. Try to delete a room that still has sensors (expect 409 Conflict)
curl -s -X DELETE http://localhost:57171/api/v1/rooms/LIB-301

# 10. Add a reading to a MAINTENANCE sensor (expect 403 Forbidden)
curl -s -X POST http://localhost:57171/api/v1/sensors/LIGHT-001/readings ^
  -H "Content-Type: application/json" ^
  -d "{\"value\":5.0}"

# 11. Register a sensor with a roomId that doesn't exist (expect 422)
curl -s -X POST http://localhost:57171/api/v1/sensors ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"TEMP-999\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":0.0,\"roomId\":\"FAKE-999\"}"
```

## Author

**Student:** Hesandi Sandesna Wickramasinghe 

**Module:** 5COSC022W – Client-Server Architectures

**University:** University of Westminster

**Year:** 2025/26
