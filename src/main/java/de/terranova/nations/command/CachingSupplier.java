package de.terranova.nations.command;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class CachingSupplier<T> implements Supplier<T> {
    private final Supplier<T> delegate;
    private final long cacheDurationNanos;
    private T cachedValue;
    private long lastUpdate;

    /**
     * @param delegate The underlying supplier that provides the fresh data.
     * @param cacheDurationMillis The duration (in milliseconds) that the cache is valid before needing a refresh.
     */
    public CachingSupplier(Supplier<T> delegate, long cacheDurationMillis) {
        this.delegate = delegate;
        this.cacheDurationNanos = TimeUnit.MILLISECONDS.toNanos(cacheDurationMillis);
        // We can start with no cachedValue and lastUpdate set to 0
    }

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