package de.terranova.nations.regions.bank;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TimestampGenerator {

    // Example in-memory cache keyed by UUID
    private static final ConcurrentMap<UUID, Long> cache = new ConcurrentHashMap<>();

    /**
     * Takes a UUID, determines a "nano timestamp" with System.nanoTime(),
     * compares it to what's in the cache:
     *  - If the cache has a value and that value >= newNanos,
     *    use cacheValue+1 instead.
     *  - Else, use the newNanos.
     * Stores this final value in the cache, then creates a Timestamp
     * (epoch-based) using the final nanos value as the fractional part.
     *
     * @param uuid The UUID key
     * @return A Timestamp with nanosecond precision in Java
     */
    public static Timestamp processUUID(UUID uuid) {
        long newNanos = System.nanoTime();

        // Get the existing nanos from the cache if any
        Long oldNanos = cache.get(uuid);

        if (oldNanos != null) {
            // If the new nanos <= old nanos, increment the old nanos by 1
            if (newNanos <= oldNanos) {
                newNanos = oldNanos + 1;
            }
        }

        // Update the cache with the final nanos
        cache.put(uuid, newNanos);

        // Create a timestamp from the epoch-based current time
        long currentMillis = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(currentMillis);

        // Use newNanos % 1,000,000,000 for the fractional second portion
        int nanoFraction = (int)(newNanos % 1_000_000_000L);
        timestamp.setNanos(nanoFraction);

        return timestamp;
    }

}
