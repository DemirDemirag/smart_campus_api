package smartcampus.support;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {
    @GET
    public Map<String, Object> discover() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name", "Smart Campus Sensor & Room Management API");
        response.put("version", "v1");
        response.put("contact", Map.of(
                "module", "5COSC022W Client-Server Architectures",
                "administrator", "Lead Backend Architect",
                "email", "smart-campus-admin@westminster.ac.uk"
        ));
        response.put("resources", Map.of(
                "rooms", "/api/v1/rooms",
                "sensors", "/api/v1/sensors"
        ));
        return response;
    }
}
