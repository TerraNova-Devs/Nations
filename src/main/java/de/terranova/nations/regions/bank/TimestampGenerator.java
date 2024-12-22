package de.terranova.nations.regions.bank;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TimestampGenerator {

        // Stores the last used microseconds timestamp for each UUID
        private static final ConcurrentMap<UUID, Long> lastTimestamps = new ConcurrentHashMap<>();

        /**
         * Generates a strictly increasing Timestamp(6) for the given UUID.
         * If the newly computed timestamp (in microseconds) is <= the last seen
         * timestamp for that UUID, it is incremented by 1 microsecond.
         *
         * @param uuid The UUID for which we want to generate a Timestamp(6).
         * @return A java.sql.Timestamp with microsecond precision.
         */
        public static synchronized Timestamp generateTimestamp(UUID uuid) {
            // 1. Get current time in microseconds since epoch
            Instant now = Instant.now();
            long currentMicros = now.getEpochSecond() * 1_000_000 + (now.getNano() / 1_000);
            // 2. Compare with the last timestamp for this UUID, if any
            Long lastUsedMicros = lastTimestamps.get(uuid);
            if (lastUsedMicros != null && currentMicros <= lastUsedMicros) {
                // Make sure we always move forward by at least 1 microsecond
                currentMicros = lastUsedMicros + 1;
            }

            // 3. Update cache
            lastTimestamps.put(uuid, currentMicros);

            // 4. Convert microseconds to a java.sql.Timestamp
            //    - seconds part
            long seconds = currentMicros / 1_000_000;
            //    - leftover micros to convert to nanos
            int nanos = (int) ((currentMicros % 1_000_000) * 1_000);

            // Create a Timestamp from the seconds, then set the nanoseconds
            Timestamp ts = new Timestamp(seconds * 1000);
            ts.setNanos(nanos);

            return ts;
        }

}
