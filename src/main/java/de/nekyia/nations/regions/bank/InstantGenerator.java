package de.nekyia.nations.regions.bank;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InstantGenerator {

    private static final Map<UUID, Instant> INSTANT_MAP = new ConcurrentHashMap<>();

    /**
     * Generates an Instant for the given UUID, ensuring strictly increasing
     * microsecond-level timestamps for each UUID.
     *
     * - Compare microsecond timestamps of the old and new Instants.
     * - If the new microsecond timestamp is <= old's microsecond timestamp,
     *   bump the old by 1 microsecond (i.e., plus 1000 nanoseconds).
     * - Otherwise, use the newly generated Instant directly.
     *
     * @param uuid the UUID for which we want an Instant
     * @return the resulting Instant (with microsecond ordering guaranteed)
     */
    public static Instant generateInstant(UUID uuid) {
        // Generate a "new" candidate Instant
        Instant newInstant = Instant.now();

        // Retrieve any previously stored Instant for this UUID
        Instant oldInstant = INSTANT_MAP.get(uuid);

        if (oldInstant != null) {
            // Compare only up to microseconds
            long oldMicros = toMicros(oldInstant);
            long newMicros = toMicros(newInstant);

            // If new is not strictly greater, bump old by 1 microsecond
            if (newMicros <= oldMicros) {
                newInstant = oldInstant.plusNanos(1000);
            }
        }

        // Store and return the final Instant
        INSTANT_MAP.put(uuid, newInstant);
        return newInstant;
    }

    /**
     * Converts an Instant to a microsecond-based timestamp since epoch.
     *
     * @param i the Instant to convert
     * @return total microseconds since the Unix epoch
     */
    private static long toMicros(Instant i) {
        return i.getEpochSecond() * 1_000_000L + (i.getNano() / 1_000);
    }
}
