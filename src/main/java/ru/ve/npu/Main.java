package ru.ve.npu;

import ru.ve.npu.sim.LoadBalancingDemo;
import ru.ve.npu.sim.NpuLoadBalancingSimulation;
import ru.ve.npu.sim.strategies.PriorityAwareAllocationStrategy;

public class Main {
    public static void main(String[] args) {
        System.out.println("NPU Modeling - Load Balancing Simulation");
        System.out.println("========================================");
        
        if (args.length > 0 && "demo".equals(args[0])) {
            // Run the full demo
            LoadBalancingDemo.main(args);
        } else {
            // Run a simple quick demo with advanced strategy
            System.out.println("Running quick demo with Priority-Aware strategy...\n");
            
            NpuLoadBalancingSimulation quickDemo = new NpuLoadBalancingSimulation(6, 
                    new PriorityAwareAllocationStrategy());
            
            // Add some sample tasks to demonstrate the system
            LoadBalancingDemo.generateSampleTasks(quickDemo, 15);
            quickDemo.runSimulation();
        }
    }
}
