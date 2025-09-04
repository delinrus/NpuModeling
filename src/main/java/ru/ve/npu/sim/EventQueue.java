package ru.ve.npu.sim;

/**
 * Interface for an event queue that orders NpuSimEvent by time and insertion order.
 */
public interface EventQueue {
    /**
     * Adds an event to the queue.
     */
    void add(NpuSimEvent event);

    /**
     * Retrieves and removes the next (earliest) event, or null if empty.
     */
    NpuSimEvent poll();

    /**
     * Peeks at the next (earliest) event without removing, or null if empty.
     */
    NpuSimEvent peek();

    /**
     * Number of events currently in the queue.
     */
    int size();

    /**
     * Whether the queue is empty.
     */
    boolean isEmpty();

    /**
     * Removes all events from the queue.
     */
    void clear();
}
