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
                .processingTimeEstimate(SimTime.ofSeconds(9))
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
