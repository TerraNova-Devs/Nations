package de.terranova.nations.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class TaskTrigger {

    private final Supplier<?> task;
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> future;

    public TaskTrigger(Supplier<?> task) {
        this.task = task;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void scheduleAt(Instant triggerTime) {
        cancel(); // cancel any previous schedule

        long delay = Duration.between(Instant.now(), triggerTime).toMillis();
        if (delay < 0) throw new IllegalArgumentException("Trigger time is in the past!");

        this.future = executor.schedule(() -> {
            try {
                task.get();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                shutdown();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    public void cancel() {
        if (future != null && !future.isDone()) {
            future.cancel(false);
        }
    }

    public boolean isScheduled() {
        return future != null && !future.isDone();
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
