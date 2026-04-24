package smartcampus.mappers;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import smartcampus.exceptions.Errors;
import smartcampus.models.ErrorResponse;

public final class ErrorHandlers {
    private ErrorHandlers() {
    }

    private abstract static class Support {
        @Context
        protected UriInfo uriInfo;

        protected Response buildResponse(Response.Status status, String message) {
            ErrorResponse body = new ErrorResponse(
                    status.getStatusCode(),
                    status.getReasonPhrase(),
                    message,
                    uriInfo == null ? "" : uriInfo.getPath()
            );
            return Response.status(status).entity(body).build();
        }

        protected Response buildResponse(int statusCode, String error, String message) {
            ErrorResponse body = new ErrorResponse(
                    statusCode,
                    error,
                    message,
                    uriInfo == null ? "" : uriInfo.getPath()
            );
            return Response.status(statusCode).entity(body).build();
        }
    }

    @Provider
    public static class RoomNotEmpty extends Support implements ExceptionMapper<Errors.RoomNotEmptyException> {
        @Override
        public Response toResponse(Errors.RoomNotEmptyException exception) {
            return buildResponse(Response.Status.CONFLICT, exception.getMessage());
        }
    }

    @Provider
    public static class LinkedResourceNotFound extends Support implements ExceptionMapper<Errors.LinkedResourceNotFoundException> {
        @Override
        public Response toResponse(Errors.LinkedResourceNotFoundException exception) {
            return buildResponse(422, "Unprocessable Entity", exception.getMessage());
        }
    }

    @Provider
    public static class SensorUnavailable extends Support implements ExceptionMapper<Errors.SensorUnavailableException> {
        @Override
        public Response toResponse(Errors.SensorUnavailableException exception) {
            return buildResponse(Response.Status.FORBIDDEN, exception.getMessage());
        }
    }

    @Provider
    public static class ResourceNotFound extends Support implements ExceptionMapper<Errors.ResourceNotFoundException> {
        @Override
        public Response toResponse(Errors.ResourceNotFoundException exception) {
            return buildResponse(Response.Status.NOT_FOUND, exception.getMessage());
        }
    }

    @Provider
    public static class Validation extends Support implements ExceptionMapper<IllegalArgumentException> {
        @Override
        public Response toResponse(IllegalArgumentException exception) {
            return buildResponse(Response.Status.BAD_REQUEST, exception.getMessage());
        }
    }

    @Provider
    public static class WebErrors extends Support implements ExceptionMapper<WebApplicationException> {
        @Override
        public Response toResponse(WebApplicationException exception) {
            Response original = exception.getResponse();
            Response.StatusType statusInfo = original.getStatusInfo();
            return buildResponse(
                    statusInfo.getStatusCode(),
                    statusInfo.getReasonPhrase(),
                    exception.getMessage() == null || exception.getMessage().isBlank()
                            ? statusInfo.getReasonPhrase()
                            : exception.getMessage()
            );
        }
    }

    @Provider
    public static class Fallback extends Support implements ExceptionMapper<Throwable> {
        private static final Logger LOGGER = Logger.getLogger(Fallback.class.getName());

        @Override
        public Response toResponse(Throwable exception) {
            LOGGER.log(Level.SEVERE, "Unhandled server error", exception);
            return buildResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    "An unexpected internal error occurred. Please contact the API administrator.");
        }
    }
}
