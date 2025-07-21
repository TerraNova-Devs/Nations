package de.terranova.nations.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class TaskTrigger {

    private static final ScheduledExecutorService SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "task‑trigger");
                t.setDaemon(true);
                return t;
            });

    private final Supplier<?> task;
    private ScheduledFuture<?> future;

    public TaskTrigger(Supplier<?> task) {
        this.task = Objects.requireNonNull(task, "task");
    }

    /**
     * Schedule the task to fire at the given moment.
     * Cancels any previous schedule for this trigger.
     *
     * @param triggerTime Instant in the future
     * @throws IllegalArgumentException if the time is in the past
     */
    public void scheduleAt(Instant triggerTime) {
        cancel();                       // clear previous schedule
        long delay = Duration.between(Instant.now(), triggerTime).toMillis();
        if (delay < 0) {
            throw new IllegalArgumentException("Trigger time is in the past: " + triggerTime);
        }

        future = SCHEDULER.schedule(() -> {
            try {
                task.get();
            } catch (Exception ex) {
                ex.printStackTrace();   // or your logging framework
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    /** Cancel this trigger if it is still pending. */
    public void cancel() {
        if (future != null && !future.isDone()) {
            future.cancel(false);
        }
    }
    
    /** @return {@code true} if this trigger is scheduled and not yet finished/cancelled. */
    public boolean isScheduled() {
        return future != null && !future.isDone();
    }

    /**
     * Returns how much time is left until this trigger is executed.
     * If no task is currently scheduled, {@link Duration#ZERO} is returned.
     *
     * @return remaining delay (never negative)
     */
    public Duration timeLeft() {
        if (future == null) {
            return Duration.ZERO;
        }
        long millis = future.getDelay(TimeUnit.MILLISECONDS);
        return Duration.ofMillis(Math.max(millis, 0L));
    }

    /**
     * Shut down the shared scheduler.
     * Call once when your application is terminating.
     */
    public static void shutdownScheduler() {
        SCHEDULER.shutdown();           // or shutdownNow() if you need it hard
    }
}
