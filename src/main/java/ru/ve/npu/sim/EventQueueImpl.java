package ru.ve.npu.sim;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Simple event queue based on PriorityQueue.
 * Orders events by time, then by insertion order as defined in NpuSimEvent.compareTo.
 */
public class EventQueueImpl implements EventQueue {
    private final Queue<NpuSimEvent> pq = new PriorityQueue<>();

    /**
     * Adds an event to the queue.
     */
    public void add(NpuSimEvent event) {
        if (event == null) throw new IllegalArgumentException("event must not be null");
        pq.add(event);
    }

    /**
     * Retrieves and removes the next (earliest) event, or null if empty.
     */
    public NpuSimEvent poll() {
        return pq.poll();
    }

    /**
     * Peeks at the next (earliest) event without removing, or null if empty.
     */
    public NpuSimEvent peek() {
        return pq.peek();
    }

    /**
     * Number of events currently in the queue.
     */
    public int size() {
        return pq.size();
    }

    /**
     * Whether the queue is empty.
     */
    public boolean isEmpty() {
        return pq.isEmpty();
    }

    /**
     * Removes all events from the queue.
     */
    public void clear() {
        pq.clear();
    }
}
