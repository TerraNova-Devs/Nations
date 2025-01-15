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
     * (epoch-based) using the final nanos value as the fractional part,
     * but only up to microsecond precision for a TIMESTAMP(6) field.
     *
     * @param uuid The UUID key
     * @return A Timestamp with microsecond precision (6 digits)
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

        // Convert newNanos to microseconds (6 digits):
        //   1 second = 1,000,000,000 ns
        //   1 second = 1,000,000 µs
        // So we divide nanoseconds by 1000 to get microseconds
        long nanoFraction = newNanos % 1_000_000_000L;
        int microFraction = (int)(nanoFraction / 1_000);

        // Store microseconds back as nanoseconds in the Timestamp
        timestamp.setNanos(microFraction * 1_000);

        return timestamp;
    }

}
