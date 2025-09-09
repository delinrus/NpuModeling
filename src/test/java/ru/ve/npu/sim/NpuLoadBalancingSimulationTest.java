package ru.ve.npu.sim;

import org.junit.jupiter.api.Test;
import ru.ve.npu.sim.strategies.FirstFitAllocationStrategy;
import ru.ve.npu.sim.strategies.LeastLoadedAllocationStrategy;

import static org.junit.jupiter.api.Assertions.*;

public class NpuLoadBalancingSimulationTest {
    
    @Test
    void testSimpleTaskAllocation() {
        // Create simulation with 4 NPUs and First Fit strategy
        NpuLoadBalancingSimulation simulation = new NpuLoadBalancingSimulation(4, 
                new FirstFitAllocationStrategy());
        
        // Create a simple task
        NpuTask task = NpuTask.builder()
                .id("test-task-1")
                .arrivalTime(SimTime.ofSeconds(1))
                .npuDemand(2)
                .npuTimeSliceRatio(0.5)
                .hbmDemand(0.3)
                .taskCompletionTime(SimTime.ofSeconds(10))
                .build();
        
        // Add task and run simulation
        simulation.addTask(task);
        simulation.runSimulation();
        
        // Verify results
        SimulationStatistics stats = simulation.getStatistics();
        assertEquals(1, stats.getTotalTasks());
        assertEquals(1, stats.getAcceptedTasks());
        assertEquals(1, stats.getCompletedTasks());
        assertEquals(0, simulation.getWaitingTasks().size());
        assertEquals(1, simulation.getCompletedTasks().size());
    }
    
    @Test
    void testTaskWaitingQueue() {
        // Create simulation with only 2 NPUs
        NpuLoadBalancingSimulation simulation = new NpuLoadBalancingSimulation(2, 
                new LeastLoadedAllocationStrategy());
        
        // Create tasks that require more NPUs than available
        NpuTask task1 = NpuTask.builder()
                .id("task-1")
                .arrivalTime(SimTime.ofSeconds(1))
                .npuDemand(2)
                .npuTimeSliceRatio(0.8)
                .hbmDemand(0.7)
                .taskCompletionTime(SimTime.ofSeconds(20))
                .build();
        
        NpuTask task2 = NpuTask.builder()
                .id("task-2")
                .arrivalTime(SimTime.ofSeconds(2))
                .npuDemand(2)
                .npuTimeSliceRatio(0.6)
                .hbmDemand(0.5)
                .taskCompletionTime(SimTime.ofSeconds(15))
                .build();
        
        // Add both tasks
        simulation.addTask(task1);
        simulation.addTask(task2);
        
        // Run simulation
        simulation.runSimulation();
        
        // Verify that one task is allocated and one completes
        SimulationStatistics stats = simulation.getStatistics();
        assertEquals(2, stats.getTotalTasks());
        assertEquals(1, stats.getAcceptedTasks()); // Only one can be allocated
        assertEquals(1, stats.getCompletedTasks());
        assertEquals(0, simulation.getWaitingTasks().size()); // Second task should be allocated after first completes
    }
    
    @Test
    void testInvalidTask() {
        NpuLoadBalancingSimulation simulation = new NpuLoadBalancingSimulation(4, 
                new FirstFitAllocationStrategy());
        
        // Create invalid task (completion before arrival)
        NpuTask invalidTask = NpuTask.builder()
                .id("invalid-task")
                .arrivalTime(SimTime.ofSeconds(10))
                .npuDemand(2)
                .npuTimeSliceRatio(0.5)
                .hbmDemand(0.3)
                .taskCompletionTime(SimTime.ofSeconds(5)) // Before arrival!
                .build();
        
        // Task should be invalid
        assertFalse(invalidTask.isValidTask());
        
        // Add task and run simulation
        simulation.addTask(invalidTask);
        simulation.runSimulation();
        
        // Invalid tasks should not be processed
        SimulationStatistics stats = simulation.getStatistics();
        assertEquals(1, stats.getTotalTasks());
        assertEquals(0, stats.getAcceptedTasks());
        assertEquals(0, stats.getCompletedTasks());
    }
    
    @Test
    void testStrategySwitch() {
        NpuLoadBalancingSimulation simulation = new NpuLoadBalancingSimulation(4, 
                new FirstFitAllocationStrategy());
        
        // Verify initial strategy
        assertEquals("First-Fit", simulation.getNpuPool().getAllocationStrategy().getStrategyName());
        
        // Switch strategy
        simulation.getNpuPool().setAllocationStrategy(new LeastLoadedAllocationStrategy());
        assertEquals("Least-Loaded", simulation.getNpuPool().getAllocationStrategy().getStrategyName());
    }
}
