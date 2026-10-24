package de.terranova.nations.heroicmap;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Hands values from one thread to another, newest first: of those offered while one was being
 * applied, only the newest is applied next, one at a time, and never an older one after a newer.
 */
final class Latest<T> {

  private final AtomicReference<T> next = new AtomicReference<>();
  private final Object applying = new Object();

  void offer(T value) {
    next.set(value);
  }

  /** Applies the newest value offered and not yet applied, if there is one. */
  void applyNewest(Consumer<T> apply) {
    synchronized (applying) {
      T value = next.getAndSet(null);
      if (value != null) {
        apply.accept(value);
      }
    }
  }
}
