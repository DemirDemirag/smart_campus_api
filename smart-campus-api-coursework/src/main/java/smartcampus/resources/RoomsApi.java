package smartcampus.resources;

import java.net.URI;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import smartcampus.models.Room;
import smartcampus.store.DataStore;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomsApi {
    private final DataStore store = DataStore.getInstance();

    @GET
    public List<Room> getRooms() {
        return store.getAllRooms();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        Room createdRoom = store.createRoom(room);
        URI location = uriInfo.getAbsolutePathBuilder().path(createdRoom.getId()).build();
        return Response.created(location)
                .entity(Map.of(
                        "message", "Room created successfully.",
                        "room", createdRoom
                ))
                .build();
    }

    @GET
    @Path("/{roomId}")
    public Room getRoom(@PathParam("roomId") String roomId) {
        return store.getRoom(roomId);
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        store.deleteRoom(roomId);
        return Response.ok(Map.of(
                "message", "Room deleted successfully.",
                "roomId", roomId
        )).build();
    }
}
