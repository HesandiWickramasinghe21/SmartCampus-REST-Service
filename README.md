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

---

## Report Answers

---

### Part 1: Service Architecture & Setup

**Q1: Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this impacts how you synchronize your in-memory data (maps/lists) to prevent race conditions.**

By default, JAX-RS creates a new instance of the resource class for every HTTP request that comes in. This is known as the per-request lifecycle. Once the request is handled and the response is sent, that instance is thrown away.

This is a problem when it comes to storing data. If I kept the rooms or sensors list as a field inside the resource class, it would be empty on every single request because a brand new object is created each time. All the data added in previous requests would be gone.

To get around this, I used a separate `DataStore` class that uses the Singleton pattern. This means there is only one instance of `DataStore` for the entire application, shared across all requests. Every resource class uses that same shared instance, so the data persists correctly.

For thread safety, I used `ConcurrentHashMap` instead of a regular `HashMap`. Since multiple requests can come in at the same time on different threads, a plain `HashMap` can cause issues like two threads inserting the same key at once. `ConcurrentHashMap` handles concurrent access safely without needing manual `synchronized` blocks everywhere.

---

**Q2: Why is the provision of "Hypermedia" (links within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this benefit client developers compared to static documentation?**

HATEOAS stands for Hypermedia as the Engine of Application State. The idea is that the API response itself tells the client where it can go next, by including links to related resources. My discovery endpoint at `GET /api/v1` does this — it returns links to `/api/v1/rooms` and `/api/v1/sensors` so the client knows what is available.

This is considered a good REST design practice because it makes the API easier to use. A client developer does not need to read through documentation to find out what URLs exist — they can just follow the links in the responses. It also means if a URL changes in a future version, clients using the embedded links will still work correctly, unlike clients that have hard-coded the paths.

The main benefit over static documentation is that the links come directly from the running server so they are always accurate. Static docs can go out of date whenever the code changes.

---

### Part 2: Room Management

**Q3: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.**

If the API only returns a list of IDs, the client would have to make a separate GET request for each ID to actually get the room details. So if there are 30 rooms, the client needs 31 requests total — one to get the IDs, and then one more per room. This is called the N+1 problem and it wastes a lot of time due to the extra network calls.

Returning the full room objects means the client gets everything in a single request. Yes, the response is bigger, but that is much better than making dozens of round trips. For a campus-scale system the extra payload size is not really a problem.

That is why my `GET /api/v1/rooms` returns the full room objects including the id, name, capacity and sensorIds all at once.

---

**Q4: Is the DELETE operation idempotent in your implementation? Provide a justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.**

Technically, my DELETE is not fully idempotent according to the HTTP spec. The spec says idempotent means sending the same request multiple times should have the same effect each time.

In my implementation, the first `DELETE /rooms/{roomId}` removes the room and returns `204 No Content`. If the same request is sent again, the room no longer exists so it returns `404 Not Found`. The status code is different between the first and second call, so it is not strictly idempotent.

However the actual state of the data ends up the same — the room is gone either way. Returning 404 on the second call is still useful because it tells the client that nothing was found, which helps catch mistakes like sending a DELETE with a wrong room ID.

---

### Part 3: Sensor Operations

**Q5 (Content Negotiation): We explicitly use the `@Consumes(MediaType.APPLICATION_JSON)` annotation. Explain the technical consequences if a client sends data as `text/plain` or `application/xml`. How does JAX-RS handle this mismatch?**

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS that the POST endpoint only accepts JSON. If a client sends a request with `Content-Type: text/plain` or `Content-Type: application/xml`, JAX-RS checks the content type before even running the method.

When it finds no matching method for that content type, it automatically returns `HTTP 415 Unsupported Media Type`. The actual method code never runs at all — no validation, no data store access, nothing. It is handled entirely by the framework.

This is useful because I do not have to write any manual checks inside the method to handle wrong content types. JAX-RS takes care of it through the annotation.

---

**Q6 (Query vs Path): Contrast using `@QueryParam` (e.g., `?type=CO2`) with putting the type in the URL path (e.g., `/sensors/type/CO2`). Why is the query parameter approach generally superior for filtering?**

Putting the filter in the path like `/sensors/type/CO2` does not make much sense because it suggests that `type` is its own sub-resource, which it is not. It also becomes messy if you want to filter by multiple things — you would end up with something like `/sensors/type/CO2/status/ACTIVE` which is hard to read and requires extra route definitions.

Using a query parameter like `?type=CO2` is the better approach for filtering because it is optional — if you leave it out you just get all sensors. You can also combine multiple filters easily like `?type=CO2&status=ACTIVE` without changing the base URL at all. The URL `/api/v1/sensors` stays the same regardless of what you are filtering by, which is cleaner and easier to work with.

---

### Part 4: Sub-Resources

**Q7 (Patterns): Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity compared to one massive controller class?**

The Sub-Resource Locator pattern lets a resource method return an instance of another class instead of a response directly. JAX-RS then uses that class to handle the rest of the URL. In my project, `SensorResource` handles everything under `/sensors` and then returns a `SensorReadingResource` object to handle `/sensors/{id}/readings`.

The main benefit is that it keeps each class focused on one thing. `SensorResource` only deals with sensors, and `SensorReadingResource` only deals with readings. If I had put everything in one class it would get very long and hard to read quickly.

It also makes it easier to add new features later. If I wanted to add `/sensors/{id}/alerts` I would just create an `AlertResource` class and add one locator method, without touching any of the existing code.

Having one giant controller class for every endpoint would be very difficult to maintain, especially as the API grows. The sub-resource pattern keeps things organised and manageable.

---

### Part 5: Error Handling & Security

**Q8 (Semantics): Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?**

`404 Not Found` means the URL the client requested does not exist on the server. But when a client sends `POST /api/v1/sensors`, that URL does exist — it is a valid endpoint. So returning 404 would confuse the client into thinking the URL is wrong, when actually the URL is fine.

The real problem is that the `roomId` inside the JSON body points to a room that does not exist. The request itself is valid, the URL is valid, the JSON is valid — the issue is with the data inside the payload.

`422 Unprocessable Entity` is more accurate here because it tells the client that the server understood the request but could not process it due to a logical problem in the body. This makes it much clearer what the client needs to fix — the data in the body, not the URL.

---

**Q9 (Cybersecurity): From a security standpoint, explain the risks of exposing internal Java stack traces to external consumers. What specific information could an attacker gather?**

Showing a raw Java stack trace in an API response is a security risk for several reasons.

First, the stack trace shows exactly which libraries and frameworks are being used, including version numbers like `jersey-server 2.39.1`. An attacker can look up known vulnerabilities for those exact versions in public databases and try to exploit them.

Second, the class names and file paths in the trace reveal the internal structure of the application. This tells an attacker how the code is organised and where potential weaknesses might be.

Third, exception messages sometimes include sensitive details like the value that caused the error, internal IDs, or configuration details that should not be public.

To prevent this, I implemented a `GlobalExceptionMapper` that catches all unhandled exceptions, logs the full details on the server side only, and sends back a generic `500 Internal Server Error` message to the client with no stack trace information at all.

---

**Q10 (Cross-cutting Concerns): Why is it advantageous to use JAX-RS Filters for logging rather than manually inserting `Logger.info()` statements inside every single method?**

If I added a `Logger.info()` call manually inside every resource method, I would have to copy the same logging code into every single method across all the resource classes. That is a lot of repeated code, and if I ever wanted to change the log format I would have to update every method individually.

There is also a risk that I might forget to add logging to a new method, which would create gaps in the logs.

Using a JAX-RS filter with `ContainerRequestFilter` and `ContainerResponseFilter` solves all of this. I write the logging logic once in one class, register it with `@Provider`, and it automatically runs for every single request and response without any extra code in the resource methods. Adding new endpoints does not require any extra work — they are logged automatically.

This approach keeps the resource classes clean and focused on their actual job, and puts all the logging in one place where it is easy to maintain.

---

## Author

**Student:** Hesandi Sandesna Wickramasinghe 

**Module:** 5COSC022W – Client-Server Architectures

**University:** University of Westminster

**Year:** 2025/26
