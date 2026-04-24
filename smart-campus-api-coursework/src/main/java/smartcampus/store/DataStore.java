package smartcampus.store;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import smartcampus.exceptions.Errors;
import smartcampus.models.Room;
import smartcampus.models.Sensor;
import smartcampus.models.SensorReading;

public final class DataStore {
    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, CopyOnWriteArrayList<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private DataStore() {
        seedData();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    public List<Room> getAllRooms() {
        return rooms.values().stream()
                .sorted(Comparator.comparing(Room::getId))
                .map(this::copyRoom)
                .collect(Collectors.toList());
    }

    public Room createRoom(Room room) {
        validateRoom(room);
        Room roomToSave = new Room(room.getId().trim(), room.getName().trim(), room.getCapacity());
        roomToSave.setSensorIds(new CopyOnWriteArrayList<>());

        Room existing = rooms.putIfAbsent(roomToSave.getId(), roomToSave);
        if (existing != null) {
            throw new IllegalArgumentException("A room with id '" + roomToSave.getId() + "' already exists.");
        }
        return copyRoom(roomToSave);
    }

    public Room getRoom(String roomId) {
        return copyRoom(findRoom(roomId));
    }

    public void deleteRoom(String roomId) {
        Room room = findRoom(roomId);
        if (!room.getSensorIds().isEmpty()) {
            throw new Errors.RoomNotEmptyException(
                    "Room '" + roomId + "' cannot be deleted because sensors are still assigned to it.");
        }
        rooms.remove(roomId);
    }

    public List<Sensor> getAllSensors(String type) {
        return sensors.values().stream()
                .filter(sensor -> type == null || type.isBlank() || sensor.getType().equalsIgnoreCase(type))
                .sorted(Comparator.comparing(Sensor::getId))
                .map(this::copySensor)
                .collect(Collectors.toList());
    }

    public Sensor createSensor(Sensor sensor) {
        validateSensor(sensor);
        Room room = rooms.get(sensor.getRoomId());
        if (room == null) {
            throw new Errors.LinkedResourceNotFoundException(
                    "Sensor cannot be created because room '" + sensor.getRoomId() + "' does not exist.");
        }

        Sensor sensorToSave = new Sensor(
                sensor.getId().trim(),
                sensor.getType().trim(),
                sensor.getStatus().trim().toUpperCase(),
                sensor.getCurrentValue(),
                sensor.getRoomId().trim()
        );

        Sensor existing = sensors.putIfAbsent(sensorToSave.getId(), sensorToSave);
        if (existing != null) {
            throw new IllegalArgumentException("A sensor with id '" + sensorToSave.getId() + "' already exists.");
        }

        room.getSensorIds().add(sensorToSave.getId());
        sensorReadings.put(sensorToSave.getId(), new CopyOnWriteArrayList<>());
        return copySensor(sensorToSave);
    }

    public Sensor getSensor(String sensorId) {
        return copySensor(findSensor(sensorId));
    }

    public List<SensorReading> getReadings(String sensorId) {
        findSensor(sensorId);
        return sensorReadings.getOrDefault(sensorId, new CopyOnWriteArrayList<>()).stream()
                .sorted(Comparator.comparingLong(SensorReading::getTimestamp))
                .map(this::copyReading)
                .collect(Collectors.toList());
    }

    public SensorReading addReading(String sensorId, SensorReading reading) {
        Sensor sensor = findSensor(sensorId);
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new Errors.SensorUnavailableException(
                    "Sensor '" + sensorId + "' is in MAINTENANCE mode and cannot accept new readings.");
        }

        validateReading(reading);
        SensorReading readingToSave = new SensorReading(
                normalizeReadingId(reading.getId()),
                reading.getTimestamp() == 0 ? System.currentTimeMillis() : reading.getTimestamp(),
                reading.getValue()
        );

        sensorReadings.computeIfAbsent(sensorId, key -> new CopyOnWriteArrayList<>()).add(readingToSave);
        sensor.setCurrentValue(readingToSave.getValue());
        return copyReading(readingToSave);
    }

    private void seedData() {
        Room library = new Room("LIB-301", "Library Quiet Study", 120);
        library.setSensorIds(new CopyOnWriteArrayList<>());
        Room lab = new Room("LAB-101", "Networking Lab", 40);
        lab.setSensorIds(new CopyOnWriteArrayList<>());
        rooms.put(library.getId(), library);
        rooms.put(lab.getId(), lab);

        Sensor co2Sensor = new Sensor("CO2-001", "CO2", "ACTIVE", 412.5, "LIB-301");
        Sensor tempSensor = new Sensor("TEMP-001", "Temperature", "MAINTENANCE", 21.0, "LAB-101");
        sensors.put(co2Sensor.getId(), co2Sensor);
        sensors.put(tempSensor.getId(), tempSensor);
        library.getSensorIds().add(co2Sensor.getId());
        lab.getSensorIds().add(tempSensor.getId());

        CopyOnWriteArrayList<SensorReading> co2History = new CopyOnWriteArrayList<>();
        co2History.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis() - 60000, 410.2));
        co2History.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis() - 10000, 412.5));
        sensorReadings.put(co2Sensor.getId(), co2History);
        sensorReadings.put(tempSensor.getId(), new CopyOnWriteArrayList<>());
    }

    private Room findRoom(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new Errors.ResourceNotFoundException("Room '" + roomId + "' was not found.");
        }
        return room;
    }

    private Sensor findSensor(String sensorId) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            throw new Errors.ResourceNotFoundException("Sensor '" + sensorId + "' was not found.");
        }
        return sensor;
    }

    private void validateRoom(Room room) {
        if (room == null || isBlank(room.getId()) || isBlank(room.getName())) {
            throw new IllegalArgumentException("Room id and name are required.");
        }
        if (room.getCapacity() <= 0) {
            throw new IllegalArgumentException("Room capacity must be greater than zero.");
        }
    }

    private void validateSensor(Sensor sensor) {
        if (sensor == null || isBlank(sensor.getId()) || isBlank(sensor.getType())
                || isBlank(sensor.getStatus()) || isBlank(sensor.getRoomId())) {
            throw new IllegalArgumentException("Sensor id, type, status and roomId are required.");
        }
        List<String> allowedStatuses = List.of("ACTIVE", "MAINTENANCE", "OFFLINE");
        if (!allowedStatuses.contains(sensor.getStatus().trim().toUpperCase())) {
            throw new IllegalArgumentException("Sensor status must be ACTIVE, MAINTENANCE or OFFLINE.");
        }
    }

    private void validateReading(SensorReading reading) {
        if (reading == null) {
            throw new IllegalArgumentException("Reading payload is required.");
        }
    }

    private String normalizeReadingId(String readingId) {
        return isBlank(readingId) ? UUID.randomUUID().toString() : readingId.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Room copyRoom(Room room) {
        Room copy = new Room(room.getId(), room.getName(), room.getCapacity());
        copy.setSensorIds(new ArrayList<>(room.getSensorIds()));
        return copy;
    }

    private Sensor copySensor(Sensor sensor) {
        return new Sensor(sensor.getId(), sensor.getType(), sensor.getStatus(), sensor.getCurrentValue(), sensor.getRoomId());
    }

    private SensorReading copyReading(SensorReading reading) {
        return new SensorReading(reading.getId(), reading.getTimestamp(), reading.getValue());
    }
}
