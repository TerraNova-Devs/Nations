package de.terranova.nations.utils.database;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UniqueTimestampGenerator {

    // Map to hold the last timestamp for each UUID
    private final Map<UUID, Long> lastTimestamps = new HashMap<>();

    /**
     * Generates a unique timestamp for a specific UUID. Ensures that timestamps are unique
     * for each UUID but can overlap across different UUIDs. (Exact up to microseconds)
     *
     * @param id The UUID for which the timestamp is generated.
     * @return A unique Timestamp with microsecond precision.
     */
    public synchronized Timestamp generate(UUID id) {
        // Get the current time in milliseconds since epoch
        long currentTimeMillis = System.currentTimeMillis();

        // Get nanoseconds for additional precision and convert to microseconds
        long currentNanoTime = System.nanoTime();
        long microTimestamp = currentTimeMillis * 1_000_000 + (currentNanoTime / 1_000) % 1_000_000;

        // Ensure uniqueness for the given UUID
        long lastMicroTimestamp = lastTimestamps.getOrDefault(id, 0L);
        if (microTimestamp <= lastMicroTimestamp) {
            microTimestamp = lastMicroTimestamp + 1;
        }

        // Update the last timestamp for this UUID
        lastTimestamps.put(id, microTimestamp);

        // Convert microseconds to a Timestamp
        long epochMillis = microTimestamp / 1_000_000;
        int microAdjustment = (int) (microTimestamp % 1_000_000);

        // Create an Instant with microsecond precision
        Instant instant = Instant.ofEpochMilli(epochMillis).plusNanos(microAdjustment * 1_000);

        return Timestamp.from(instant);
    }

}
