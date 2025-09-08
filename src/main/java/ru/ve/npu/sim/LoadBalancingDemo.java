package ru.ve.npu.sim;

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
        runSimulationWithStrategy(NpuPool.LoadBalancingStrategy.FIRST_FIT);
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        runSimulationWithStrategy(NpuPool.LoadBalancingStrategy.LEAST_LOADED);
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        runSimulationWithStrategy(NpuPool.LoadBalancingStrategy.BEST_FIT);
    }
    
    private static void runSimulationWithStrategy(NpuPool.LoadBalancingStrategy strategy) {
        System.out.println("Running simulation with strategy: " + strategy);
        System.out.println("-".repeat(40));
        
        // Create simulation with 8 NPUs
        NpuLoadBalancingSimulation simulation = new NpuLoadBalancingSimulation(8, strategy);
        
        // Generate sample tasks
        generateSampleTasks(simulation, 20);
        
        // Run the simulation
        simulation.runSimulation();
    }
    
    private static void generateSampleTasks(NpuLoadBalancingSimulation simulation, int taskCount) {
        System.out.println("Generating " + taskCount + " sample tasks...\n");
        
        double currentTime = 0.0;
        
        for (int i = 0; i < taskCount; i++) {
            // Generate task parameters
            String taskId = "Task-" + String.format("%03d", i + 1);
            
            // Arrival time: exponential inter-arrival times (average 5.0 time units)
            double interArrivalTime = -Math.log(random.nextDouble()) * 5.0;
            currentTime += interArrivalTime;
            
            // NPU demand: 1-4 NPUs
            int npuDemand = 1 + random.nextInt(4);
            
            // NPU time slice ratio: 20% to 80%
            double npuTimeSliceRatio = 0.2 + random.nextDouble() * 0.6;
            
            // HBM demand: 10% to 60%
            double hbmDemand = 0.1 + random.nextDouble() * 0.5;
            
            // Task duration: 10 to 50 time units
            double taskDuration = 10.0 + random.nextDouble() * 40.0;
            double completionTime = currentTime + taskDuration;
            
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
                NpuPool.LoadBalancingStrategy.LEAST_LOADED);
        
        // Create tasks that will stress the system
        for (int i = 0; i < 10; i++) {
            NpuTask task = NpuTask.builder()
                    .id("HighLoad-" + i)
                    .arrivalTime(i * 2.0) // Tasks arrive every 2 time units
                    .npuDemand(3) // Each task needs 3 NPUs
                    .npuTimeSliceRatio(0.8) // High compute usage
                    .hbmDemand(0.7) // High memory usage
                    .taskCompletionTime(i * 2.0 + 25.0) // 25 time units duration
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
                NpuPool.LoadBalancingStrategy.ROUND_ROBIN);
        
        // Small tasks
        for (int i = 0; i < 5; i++) {
            NpuTask task = NpuTask.builder()
                    .id("Small-" + i)
                    .arrivalTime(i * 1.0)
                    .npuDemand(1)
                    .npuTimeSliceRatio(0.3)
                    .hbmDemand(0.2)
                    .taskCompletionTime(i * 1.0 + 5.0)
                    .build();
            simulation.addTask(task);
        }
        
        // Large tasks
        for (int i = 0; i < 3; i++) {
            NpuTask task = NpuTask.builder()
                    .id("Large-" + i)
                    .arrivalTime(10.0 + i * 5.0)
                    .npuDemand(4)
                    .npuTimeSliceRatio(0.9)
                    .hbmDemand(0.8)
                    .taskCompletionTime(10.0 + i * 5.0 + 30.0)
                    .build();
            simulation.addTask(task);
        }
        
        simulation.runSimulation();
    }
}

