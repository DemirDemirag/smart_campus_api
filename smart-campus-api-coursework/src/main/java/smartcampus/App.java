package smartcampus;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import smartcampus.mappers.ErrorHandlers;
import smartcampus.resources.RoomsApi;
import smartcampus.resources.SensorsApi;
import smartcampus.support.ApiLoggingFilter;
import smartcampus.support.DiscoveryResource;

public final class App {
    private static final URI BASE_URI = URI.create("http://0.0.0.0:8080/");

    private App() {
    }

    public static void main(String[] args) throws IOException {
        ResourceConfig config = ResourceConfig.forApplication(new AppConfig());
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, config);
        CountDownLatch shutdownLatch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdownNow();
            shutdownLatch.countDown();
        }));
        System.out.println("Smart Campus API running at http://localhost:8080/api/v1/");
        System.out.println("Press Ctrl+C to stop the server.");
        try {
            shutdownLatch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}

@ApplicationPath("/api/v1")
class AppConfig extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(DiscoveryResource.class);
        classes.add(RoomsApi.class);
        classes.add(SensorsApi.class);
        classes.add(ApiLoggingFilter.class);
        classes.add(ErrorHandlers.RoomNotEmpty.class);
        classes.add(ErrorHandlers.LinkedResourceNotFound.class);
        classes.add(ErrorHandlers.SensorUnavailable.class);
        classes.add(ErrorHandlers.ResourceNotFound.class);
        classes.add(ErrorHandlers.Validation.class);
        classes.add(ErrorHandlers.WebErrors.class);
        classes.add(ErrorHandlers.Fallback.class);
        return classes;
    }
}
