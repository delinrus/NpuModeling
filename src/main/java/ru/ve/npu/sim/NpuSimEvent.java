package ru.ve.npu.sim;

import lombok.Getter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Abstract timed event for NPU simulation.
 * Two kinds of events are expected: ARRIVAL and COMPLETION.
 * Instances are intended to be stored in a priority queue ordered by event time
 * (and then by insertion order to keep FIFO for simultaneous events).
 */
@Getter
public abstract class NpuSimEvent implements Comparable<NpuSimEvent> {

    /**
     * Event type.
     */
    public enum Type {
        ARRIVAL,
        COMPLETION
    }

    /**
     * Simulation timestamp of the event using SimTime.
     */
    private final SimTime time;

    /**
     * Type of the event.
     */
    private final Type type;

    /**
     * Monotonic sequence number to preserve insertion order for equal timestamps.
     */
    private final long seq;

    private static final AtomicLong SEQ_GEN = new AtomicLong(0L);

    protected NpuSimEvent(SimTime time, Type type) {
        this.time = time;
        this.type = type;
        this.seq = SEQ_GEN.incrementAndGet();
    }

    /**
     * Executes the event-specific logic against the provided simulation context.
     * The context type is left generic so that concrete simulations can define
     * their own data and behavior.
     */
    public abstract void process(Object context);

    @Override
    public int compareTo(NpuSimEvent other) {
        int byTime = this.time.compareTo(other.time);
        if (byTime != 0) return byTime;
        // Stable ordering for same time: earlier inserted first
        return Long.compare(this.seq, other.seq);
    }

    @Override
    public String toString() {
        return "NpuSimEvent{" +
                "time=" + time +
                ", type=" + type +
                ", seq=" + seq +
                '}';
    }
}
