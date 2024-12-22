package de.terranova.nations.regions.bank;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class MonotonicTimestampGen {

    // For each UUID, we store the last used microsecond timestamp (epoch-based).
    private static final ConcurrentMap<UUID, AtomicLong> LAST_TIMESTAMP_MICROS = new ConcurrentHashMap<>();

    /**
     * Generates a strictly increasing java.sql.Timestamp with microsecond granularity,
     * scoped per UUID. If calls happen faster than the OS clock updates (e.g., multiple
     * calls in the same millisecond), microseconds are incremented artificially:
     *   first call might end in .000000, next in .000001, etc.
     *
     * @param id the UUID whose timestamp sequence we are generating
     * @return a strictly increasing Timestamp for the given UUID
     */
    public static Timestamp generateTimestamp(UUID id) {
        // Get current time with potential sub-millisecond accuracy (depends on OS/hardware)
        Instant now = Instant.now();

        // Convert Instant to epoch-based microseconds
        long currentTimeMicros = now.getEpochSecond() * 1_000_000L + (now.getNano() / 1_000);

        // Atomically ensure a strictly increasing microsecond value for this UUID
        long finalMicros = LAST_TIMESTAMP_MICROS
                .computeIfAbsent(id, k -> new AtomicLong(0L))
                .updateAndGet(prev -> {
                    if (currentTimeMicros <= prev) {
                        // If time hasn't advanced, just increment the previous by 1 µs
                        return prev + 1;
                    } else {
                        return currentTimeMicros;
                    }
                });

        // Convert finalMicros to java.sql.Timestamp
        long millis = finalMicros / 1_000;
        int nanos  = (int)((finalMicros % 1_000_000) * 1_000);

        Timestamp ts = new Timestamp(millis);
        ts.setNanos(nanos);
        return ts;
    }

}
