package ru.ve.npu.sim;

import org.junit.jupiter.api.Test;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class SimTimeTest {
    
    @Test
    void testBasicOperations() {
        SimTime t1 = SimTime.ofSeconds(5);
        SimTime t2 = SimTime.ofSeconds(3);
        
        assertEquals(SimTime.ofSeconds(8), t1.plus(t2));
        assertEquals(SimTime.ofSeconds(2), t1.minus(t2));
        assertEquals(SimTime.ofSeconds(10), t1.multiply(2));
        assertEquals(SimTime.ofSeconds(2.5), t1.divide(2));
    }
    
    @Test
    void testComparisons() {
        SimTime t1 = SimTime.ofSeconds(5);
        SimTime t2 = SimTime.ofSeconds(3);
        SimTime t3 = SimTime.ofSeconds(5);
        
        assertTrue(t1.isAfter(t2));
        assertFalse(t1.isBefore(t2));
        assertTrue(t1.isAfterOrEqual(t3));
        assertTrue(t1.isBeforeOrEqual(t3));
        assertFalse(t1.isAfter(t3));
        assertFalse(t1.isBefore(t3));
    }
    
    @Test
    void testConversions() {
        SimTime t = SimTime.ofSeconds(1.5);
        
        assertEquals(1_500_000_000L, t.toNanos());
        assertEquals(1_500_000L, t.toMicros());
        assertEquals(1500L, t.toMillis());
        assertEquals(1L, t.toSeconds());
        assertEquals(1.5, t.toSecondsDouble(), 0.001);
        
        assertEquals(Duration.ofNanos(1_500_000_000L), t.toDuration());
    }
    
    @Test
    void testZeroAndSpecialValues() {
        SimTime zero = SimTime.ZERO;
        SimTime positive = SimTime.ofSeconds(5);
        SimTime negative = SimTime.ofNanos(-1000);
        
        assertTrue(zero.isZero());
        assertTrue(positive.isPositive());
        assertTrue(negative.isNegative());
        
        assertEquals(SimTime.ofNanos(1000), negative.abs());
    }
    
    @Test
    void testMinMax() {
        SimTime t1 = SimTime.ofSeconds(3);
        SimTime t2 = SimTime.ofSeconds(7);
        
        assertEquals(t1, t1.min(t2));
        assertEquals(t2, t1.max(t2));
    }
    
    @Test
    void testToString() {
        assertEquals("0s", SimTime.ZERO.toString());
        assertEquals("500ns", SimTime.ofNanos(500).toString());
        assertEquals("1,500μs", SimTime.ofNanos(1500).toString());
        assertEquals("2,500ms", SimTime.ofNanos(2_500_000).toString());
        assertEquals("3,500s", SimTime.ofNanos(3_500_000_000L).toString());
    }
}
