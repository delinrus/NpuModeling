package ru.ve.npu.sim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NpuTaskTest {
    
    @Test
    void testValidTask() {
        NpuTask validTask = NpuTask.builder()
                .id("task-1")
                .arrivalTime(SimTime.ofSeconds(10))
                .npuDemand(2)
                .npuTimeSliceRatio(0.5)
                .hbmDemand(0.3)
                .taskCompletionTime(SimTime.ofSeconds(15))
                .build();
        
        assertTrue(validTask.isValidTask());
        assertEquals(SimTime.ofSeconds(5), validTask.getDuration());
        assertEquals(1.0, validTask.getTotalComputeUnits());
        assertEquals(0.6, validTask.getTotalHbmUnits());
    }
    
    @Test
    void testInvalidTasks() {
        // Invalid NPU demand
        NpuTask invalidNpuDemand = NpuTask.builder()
                .id("task-1")
                .arrivalTime(SimTime.ofSeconds(10))
                .npuDemand(0)
                .npuTimeSliceRatio(0.5)
                .hbmDemand(0.3)
                .taskCompletionTime(SimTime.ofSeconds(15))
                .build();
        assertFalse(invalidNpuDemand.isValidTask());
        
        // Invalid time slice ratio
        NpuTask invalidTimeSlice = NpuTask.builder()
                .id("task-2")
                .arrivalTime(SimTime.ofSeconds(10))
                .npuDemand(2)
                .npuTimeSliceRatio(1.5) // > 1.0
                .hbmDemand(0.3)
                .taskCompletionTime(SimTime.ofSeconds(15))
                .build();
        assertFalse(invalidTimeSlice.isValidTask());
        
        // Invalid HBM demand
        NpuTask invalidHbm = NpuTask.builder()
                .id("task-3")
                .arrivalTime(SimTime.ofSeconds(10))
                .npuDemand(2)
                .npuTimeSliceRatio(0.5)
                .hbmDemand(-0.1) // < 0.0
                .taskCompletionTime(SimTime.ofSeconds(15))
                .build();
        assertFalse(invalidHbm.isValidTask());
        
        // Completion time before arrival time
        NpuTask invalidTiming = NpuTask.builder()
                .id("task-4")
                .arrivalTime(SimTime.ofSeconds(15))
                .npuDemand(2)
                .npuTimeSliceRatio(0.5)
                .hbmDemand(0.3)
                .taskCompletionTime(SimTime.ofSeconds(10)) // before arrival
                .build();
        assertFalse(invalidTiming.isValidTask());
    }
    
    @Test
    void testTaskEquality() {
        NpuTask task1 = NpuTask.builder()
                .id("task-1")
                .arrivalTime(SimTime.ofSeconds(10))
                .npuDemand(2)
                .npuTimeSliceRatio(0.5)
                .hbmDemand(0.3)
                .taskCompletionTime(SimTime.ofSeconds(15))
                .build();
        
        NpuTask task2 = NpuTask.builder()
                .id("task-1") // same ID
                .arrivalTime(SimTime.ofSeconds(20))
                .npuDemand(3)
                .npuTimeSliceRatio(0.8)
                .hbmDemand(0.7)
                .taskCompletionTime(SimTime.ofSeconds(30))
                .build();
        
        NpuTask task3 = NpuTask.builder()
                .id("task-3") // different ID
                .arrivalTime(SimTime.ofSeconds(10))
                .npuDemand(2)
                .npuTimeSliceRatio(0.5)
                .hbmDemand(0.3)
                .taskCompletionTime(SimTime.ofSeconds(15))
                .build();
        
        assertEquals(task1, task2); // same ID
        assertNotEquals(task1, task3); // different ID
        assertEquals(task1.hashCode(), task2.hashCode());
        assertNotEquals(task1.hashCode(), task3.hashCode());
    }
}
