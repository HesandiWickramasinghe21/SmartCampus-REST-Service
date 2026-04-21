Part 1: Service Architecture & Setup
Q1 : Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this impacts how you synchronize your in-memory data (maps/lists) to prevent race conditions.

Q2 : Why is the provision of ”Hypermedia” (links within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this benefit client developers compared to static documentation?


Part 2: Room Management
Q3 : When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? (Consider network bandwidth and client-side processing).

Q4 : Is the DELETE operation idempotent in your implementation? Provide a justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.


Part 3: Sensor Operations
Q5 (Content Negotiation): We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation. Explain the technical consequences if a client sends data as text/plain or application/xml. How does JAX-RS handle this mismatch?

Q6 (Query vs Path): Contrast using @QueryParam (e.g., ?type=CO2) with putting the type in the URL path (e.g., /sensors/type/CO2). Why is the query parameter approach generally superior for filtering?


Part 4: Sub-Resources
Q7 (Patterns): Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity compared to one massive controller class?


Part 5: Error Handling & Security
Q8 (Semantics): Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

Q9 (Cybersecurity): From a security standpoint, explain the risks of exposing internal Java stack traces to external consumers. What specific information could an attacker gather?

Q10 (Cross-cutting Concerns): Why is it advantageous to use JAX-RS Filters for logging rather than manually inserting Logger.info() statements inside every single method?