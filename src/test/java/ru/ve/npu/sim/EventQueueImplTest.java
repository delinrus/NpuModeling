package ru.ve.npu.sim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventQueueImplTest {

    // Simple concrete event for testing
    static class TestEvent extends NpuSimEvent {
        final String id;

        TestEvent(double time, Type type, String id) {
            super(time, type);
            this.id = id;
        }

        @Override
        public void process(Object context) { /* no-op */ }

        @Override
        public String toString() {
            return id + "@" + getTime();
        }
    }

    @Test
    void ordersByTimeAscending() {
        EventQueue q = new EventQueueImpl();
        q.add(new TestEvent(10.0, NpuSimEvent.Type.ARRIVAL, "e10"));
        q.add(new TestEvent(5.0, NpuSimEvent.Type.COMPLETION, "e5"));
        q.add(new TestEvent(7.5, NpuSimEvent.Type.ARRIVAL, "e7.5"));

        assertEquals(3, q.size());
        assertEquals("e5", ((TestEvent) q.poll()).id);
        assertEquals("e7.5", ((TestEvent) q.poll()).id);
        assertEquals("e10", ((TestEvent) q.poll()).id);
        assertTrue(q.isEmpty());
    }

    @Test
    void keepsInsertionOrderForSameTime() throws InterruptedException {
        EventQueue q = new EventQueueImpl();
        TestEvent a = new TestEvent(3.0, NpuSimEvent.Type.ARRIVAL, "a");
        TestEvent b = new TestEvent(3.0, NpuSimEvent.Type.COMPLETION, "b");
        TestEvent c = new TestEvent(3.0, NpuSimEvent.Type.ARRIVAL, "c");
        q.add(a);
        q.add(b);
        q.add(c);

        assertEquals("a", ((TestEvent) q.poll()).id);
        assertEquals("b", ((TestEvent) q.poll()).id);
        assertEquals("c", ((TestEvent) q.poll()).id);
    }

    @Test
    void peekDoesNotRemove() {
        EventQueue q = new EventQueueImpl();
        TestEvent a = new TestEvent(1.0, NpuSimEvent.Type.ARRIVAL, "a");
        TestEvent b = new TestEvent(2.0, NpuSimEvent.Type.ARRIVAL, "b");
        q.add(b);
        q.add(a);

        assertEquals("a", ((TestEvent) q.peek()).id);
        assertEquals(2, q.size(), "peek must not remove element");
        assertEquals("a", ((TestEvent) q.poll()).id);
        assertEquals("b", ((TestEvent) q.poll()).id);
    }

    @Test
    void pollAndPeekOnEmptyReturnNull() {
        EventQueue q = new EventQueueImpl();
        assertTrue(q.isEmpty());
        assertNull(q.peek());
        assertNull(q.poll());
    }

    @Test
    void clearEmptiesQueue() {
        EventQueue q = new EventQueueImpl();
        q.add(new TestEvent(1.0, NpuSimEvent.Type.ARRIVAL, "a"));
        q.add(new TestEvent(2.0, NpuSimEvent.Type.ARRIVAL, "b"));
        assertEquals(2, q.size());
        q.clear();
        assertTrue(q.isEmpty());
        assertNull(q.poll());
    }
}
