package ru.ve.npu.sim;

import ru.ve.npu.sim.strategies.*;
import java.util.Random;

/**
 * Demonstration of NPU load balancing simulation.
 * Shows how to create tasks and run the simulation with different strategies.
 */
public class LoadBalancingDemo {
    
    private static final Random random = new Random(42); // Fixed seed for reproducibility
    
    public static void main(String[] args) {
        System.out.println("NPU Load Balancing Simulation Demo");
        System.out.println("==================================\n");
        
        // Run simulation with different strategies
        runSimulationWithStrategy(new FirstFitAllocationStrategy());
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        runSimulationWithStrategy(new LeastLoadedAllocationStrategy());
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        runSimulationWithStrategy(new BestFitAllocationStrategy());
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        runSimulationWithStrategy(new PriorityAwareAllocationStrategy());
    }
    
    private static void runSimulationWithStrategy(NpuAllocationStrategy strategy) {
        System.out.println("Running simulation with strategy: " + strategy.getStrategyName());
        System.out.println("-".repeat(40));
        
        // Create simulation with 8 NPUs
        NpuLoadBalancingSimulation simulation = new NpuLoadBalancingSimulation(8, strategy);
        
        // Generate sample tasks
        generateSampleTasks(simulation, 20);
        
        // Run the simulation
        simulation.runSimulation();
    }
    
    public static void generateSampleTasks(NpuLoadBalancingSimulation simulation, int taskCount) {
        System.out.println("Generating " + taskCount + " sample tasks...\n");
        
        SimTime currentTime = SimTime.ZERO;
        
        for (int i = 0; i < taskCount; i++) {
            // Generate task parameters
            String taskId = "Task-" + String.format("%03d", i + 1);
            
            // Arrival time: exponential inter-arrival times (average 5.0 seconds)
            double interArrivalSeconds = -Math.log(random.nextDouble()) * 5.0;
            currentTime = currentTime.plusSeconds(interArrivalSeconds);
            
            // NPU demand: 1-4 NPUs
            int npuDemand = 1 + random.nextInt(4);
            
            // NPU time slice ratio: 20% to 80%
            double npuTimeSliceRatio = 0.2 + random.nextDouble() * 0.6;
            
            // HBM demand: 10% to 60%
            double hbmDemand = 0.1 + random.nextDouble() * 0.5;
            
            // Task duration: 10 to 50 seconds
            double taskDurationSeconds = 10.0 + random.nextDouble() * 40.0;
            SimTime completionTime = currentTime.plusSeconds(taskDurationSeconds);
            
            // Create and add task
            NpuTask task = NpuTask.builder()
                    .id(taskId)
                    .arrivalTime(currentTime)
                    .npuDemand(npuDemand)
                    .npuTimeSliceRatio(npuTimeSliceRatio)
                    .hbmDemand(hbmDemand)
                    .taskCompletionTime(completionTime)
                    .build();
            
            simulation.addTask(task);
        }
    }
    
    /**
     * Creates a high-load scenario for testing.
     */
    public static void runHighLoadScenario() {
        System.out.println("High Load Scenario Test");
        System.out.println("=======================");
        
        NpuLoadBalancingSimulation simulation = new NpuLoadBalancingSimulation(4, 
                new LeastLoadedAllocationStrategy());
        
        // Create tasks that will stress the system
        for (int i = 0; i < 10; i++) {
            SimTime arrivalTime = SimTime.ofSeconds(i * 2.0); // Tasks arrive every 2 seconds
            SimTime completionTime = arrivalTime.plusSeconds(25.0); // 25 seconds duration
            
            NpuTask task = NpuTask.builder()
                    .id("HighLoad-" + i)
                    .arrivalTime(arrivalTime)
                    .npuDemand(3) // Each task needs 3 NPUs
                    .npuTimeSliceRatio(0.8) // High compute usage
                    .hbmDemand(0.7) // High memory usage
                    .taskCompletionTime(completionTime)
                    .build();
            
            simulation.addTask(task);
        }
        
        simulation.runSimulation();
    }
    
    /**
     * Creates a mixed workload scenario.
     */
    public static void runMixedWorkloadScenario() {
        System.out.println("Mixed Workload Scenario Test");
        System.out.println("============================");
        
        NpuLoadBalancingSimulation simulation = new NpuLoadBalancingSimulation(6, 
                new RoundRobinAllocationStrategy());
        
        // Small tasks
        for (int i = 0; i < 5; i++) {
            SimTime arrivalTime = SimTime.ofSeconds(i * 1.0);
            SimTime completionTime = arrivalTime.plusSeconds(5.0);
            
            NpuTask task = NpuTask.builder()
                    .id("Small-" + i)
                    .arrivalTime(arrivalTime)
                    .npuDemand(1)
                    .npuTimeSliceRatio(0.3)
                    .hbmDemand(0.2)
                    .taskCompletionTime(completionTime)
                    .build();
            simulation.addTask(task);
        }
        
        // Large tasks
        for (int i = 0; i < 3; i++) {
            SimTime arrivalTime = SimTime.ofSeconds(10.0 + i * 5.0);
            SimTime completionTime = arrivalTime.plusSeconds(30.0);
            
            NpuTask task = NpuTask.builder()
                    .id("Large-" + i)
                    .arrivalTime(arrivalTime)
                    .npuDemand(4)
                    .npuTimeSliceRatio(0.9)
                    .hbmDemand(0.8)
                    .taskCompletionTime(completionTime)
                    .build();
            simulation.addTask(task);
        }
        
        simulation.runSimulation();
    }
}

