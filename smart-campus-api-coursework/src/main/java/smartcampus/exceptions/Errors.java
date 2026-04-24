package smartcampus.exceptions;

public final class Errors {
    private Errors() {
    }

    public static class RoomNotEmptyException extends RuntimeException {
        public RoomNotEmptyException(String message) {
            super(message);
        }
    }

    public static class LinkedResourceNotFoundException extends RuntimeException {
        public LinkedResourceNotFoundException(String message) {
            super(message);
        }
    }

    public static class SensorUnavailableException extends RuntimeException {
        public SensorUnavailableException(String message) {
            super(message);
        }
    }

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}
