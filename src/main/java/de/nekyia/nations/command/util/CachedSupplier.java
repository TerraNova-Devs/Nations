package de.nekyia.nations.command.util;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A supplier implementation that caches the result from a delegate supplier for a specified duration.
 * This class can be used to prevent frequent expensive computations or I/O operations by caching
 * the result and reusing it for a defined time period.
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * // Create an expensive supplier, e.g., fetching data from a database
 * Supplier<Data> expensiveSupplier = () -> fetchDataFromDatabase();
 *
 * // Wrap it with CachedSupplier to cache the result for 5 seconds
 * CachedSupplier<Data> cachedSupplier = new CachedSupplier<>(expensiveSupplier, 5000);
 *
 * // Use the cached supplier
 * Data data = cachedSupplier.get(); // Fetches fresh data if cache expired
 * }</pre>
 *
 * @param <T> the type of results supplied by this supplier
 */
public class CachedSupplier<T> implements Supplier<T> {
    private final Supplier<T> delegate;
    private final long cacheDurationNanos;
    private T cachedValue;
    private long lastUpdate;

    /**
     * Constructs a new CachedSupplier that caches values from the given delegate supplier
     * for the specified cache duration.
     *
     * @param delegate            The underlying supplier that provides the fresh data.
     * @param cacheDurationMillis The duration (in milliseconds) that the cache is valid before needing a refresh.
     */
    public CachedSupplier(Supplier<T> delegate, long cacheDurationMillis) {
        this.delegate = delegate;
        this.cacheDurationNanos = TimeUnit.MILLISECONDS.toNanos(cacheDurationMillis);
        // We can start with no cachedValue and lastUpdate set to 0
    }

    /**
     * Returns the cached value if it is still valid; otherwise, obtains a fresh value from the delegate supplier,
     * updates the cache, and returns the new value.
     *
     * @return The cached or freshly supplied value.
     */
    @Override
    public T get() {
        long now = System.nanoTime();
        // If no cached value yet or the cache is older than cacheDurationNanos, refresh it.
        if (cachedValue == null || (now - lastUpdate) > cacheDurationNanos) {
            cachedValue = delegate.get();
            lastUpdate = now;
        }
        return cachedValue;
    }
}