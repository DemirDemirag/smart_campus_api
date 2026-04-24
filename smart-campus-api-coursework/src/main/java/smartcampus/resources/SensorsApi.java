package smartcampus.resources;

import java.net.URI;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import smartcampus.models.Sensor;
import smartcampus.models.SensorReading;
import smartcampus.store.DataStore;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorsApi {
    private final DataStore store = DataStore.getInstance();

    @GET
    public List<Sensor> getSensors(@QueryParam("type") String type) {
        return store.getAllSensors(type);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        Sensor createdSensor = store.createSensor(sensor);
        URI location = uriInfo.getAbsolutePathBuilder().path(createdSensor.getId()).build();
        return Response.created(location)
                .entity(Map.of(
                        "message", "Sensor created successfully.",
                        "sensor", createdSensor
                ))
                .build();
    }

    @GET
    @Path("/{sensorId}")
    public Sensor getSensor(@PathParam("sensorId") String sensorId) {
        return store.getSensor(sensorId);
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource sensorReadings(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId, store);
    }

    @Produces(MediaType.APPLICATION_JSON)
    public static class SensorReadingResource {
        private final String sensorId;
        private final DataStore store;

        public SensorReadingResource(String sensorId, DataStore store) {
            this.sensorId = sensorId;
            this.store = store;
        }

        @GET
        public List<SensorReading> getReadings() {
            return store.getReadings(sensorId);
        }

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        public Response addReading(SensorReading reading, @Context UriInfo uriInfo) {
            SensorReading createdReading = store.addReading(sensorId, reading);
            URI location = uriInfo.getAbsolutePathBuilder().path(createdReading.getId()).build();
            return Response.created(location)
                    .entity(Map.of(
                            "message", "Sensor reading stored successfully.",
                            "reading", createdReading,
                            "sensorId", sensorId
                    ))
                    .build();
        }
    }
}
