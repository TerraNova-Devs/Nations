package de.nekyia.nations.command.util;

import de.nekyia.nations.command.PlayerAwarePlaceholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A PlayerAwarePlaceholder that caches the results of a Function<UUID, List<String>>
 * for a specified duration (in milliseconds). Each player (UUID) maintains its own
 * cache entry.
 */
public class CachedUUIDPlaceholder implements PlayerAwarePlaceholder {

    private final Function<UUID, List<String>> delegate;
    private final long cacheDurationNanos;

    /**
     * Per-player cache of computed suggestions.
     * Key = UUID, Value = last computed suggestions
     */
    private final Map<UUID, List<String>> cache = new HashMap<>();

    /**
     * Records the last time (in nanoseconds) that we computed suggestions for each UUID.
     */
    private final Map<UUID, Long> lastUpdate = new HashMap<>();

    /**
     * Constructs a new CachedUUIDPlaceholder.
     *
     * @param delegate            The underlying Function that returns suggestions for a given UUID.
     * @param cacheDurationMillis How long (in milliseconds) each UUID’s cached suggestions remain valid.
     */
    public CachedUUIDPlaceholder(Function<UUID, List<String>> delegate, long cacheDurationMillis) {
        this.delegate = delegate;
        this.cacheDurationNanos = TimeUnit.MILLISECONDS.toNanos(cacheDurationMillis);
    }

    @Override
    public List<String> getSuggestions(CommandSender sender) {
        // If the sender is not a Player, there's no UUID, so return empty
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        UUID uuid = player.getUniqueId();
        long now = System.nanoTime();
        Long last = lastUpdate.get(uuid);

        // If we've never cached for this UUID OR cache is expired, recompute
        if (last == null || (now - last) > cacheDurationNanos) {
            List<String> newValue = delegate.apply(uuid);
            cache.put(uuid, newValue);
            lastUpdate.put(uuid, now);
            return newValue;
        }

        // Otherwise, just return the cached value
        return cache.get(uuid);
    }
}
