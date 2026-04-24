# Smart Campus API

This is my coursework project for 5COSC022W.

It is a small REST API for:
- rooms
- sensors
- sensor readings

The data is stored in memory using Java collections.

## How to run it

You need:
- JDK 17 or above
- Maven

Open the project folder in a terminal.

First run `mvn clean compile`.

After that run `mvn exec:java`.

The API should start on `http://localhost:8080/api/v1/`.

## Quick checks

You can test the API in a browser or in Postman.

To check the discovery endpoint, open:
`http://localhost:8080/api/v1`

To check the rooms list, open:
`http://localhost:8080/api/v1/rooms`

To check filtered sensors, open:
`http://localhost:8080/api/v1/sensors?type=CO2`

## Report answers

### 1.1
In JAX-RS, a new resource object is usually created for each request. This is good because request data is not shared between users. The problem is that if I stored my room and sensor data inside that object, it would disappear after the request ends. Because of that, I used one shared in-memory store with thread-safe collections.

### 1.2
Hypermedia helps the client know where to go next by giving links in the response. This is useful because the client does not have to depend only on fixed documentation. It also makes the API easier to change later.

### 2.1
If the API only returns room IDs, the response is smaller and faster. But then the client has to send more requests to get the full room information. Returning full room objects is easier for the client, but the response becomes bigger.

### 2.2
The delete operation is idempotent because after the room is deleted once, sending the same delete request again does not change the final state. In my API, the first delete works, and later ones may return 404, but the room is still already deleted.

### 3.1
The annotation that says the endpoint accepts JSON means the client must send JSON data. If the client sends plain text or XML instead, JAX-RS can reject it with 415 Unsupported Media Type. This helps protect the API from wrong input.

### 3.2
Query parameters are better for filtering because the client is still asking for the same list, just with a condition added. For example, type=CO2 means only show sensors of that type. This is clearer than putting the filter inside the path.

### 4.1
The sub-resource locator pattern helps keep the code organised. In this project, the main sensor endpoints stay in one class, and the reading endpoints are handled separately. This makes the code easier to read and manage.

### 4.2
When a new reading is added, it is saved in the reading history and it also updates the current value of the sensor. I did this so the data stays consistent across the API.

### 5.1
If someone tries to delete a room that still has sensors in it, the API returns 409 Conflict. I used a custom exception mapper for this because the request is valid, but it breaks a rule in the system.

### 5.2
422 Unprocessable Entity makes sense here because the JSON itself is valid, but it contains a room reference that does not exist. I think this is more accurate than 404 because the problem is in the request body, not in the URL.

### 5.3
If a sensor is in MAINTENANCE, the API does not allow a new reading to be added. That is why it returns 403 Forbidden.

### 5.4
Returning raw stack traces is dangerous because they can show internal details like class names, method names, file paths, and framework details. That information could help an attacker. A simple 500 error message is safer.

### 5.5
Using filters for logging is better than writing logging code inside every endpoint. The same logging code can run for every request and response automatically, which keeps the code cleaner.
