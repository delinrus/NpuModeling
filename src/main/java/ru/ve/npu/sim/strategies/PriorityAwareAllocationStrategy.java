package ru.ve.npu.sim.strategies;

import ru.ve.npu.sim.Npu;
import ru.ve.npu.sim.NpuAllocationStrategy;
import ru.ve.npu.sim.NpuTask;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Priority-aware allocation strategy.
 * Demonstrates advanced allocation logic that considers task priority and resource efficiency.
 * 
 * This strategy:
 * 1. Prioritizes tasks with higher resource demands (assuming they are more important)
 * 2. Uses a hybrid approach: least-loaded for large tasks, best-fit for small tasks
 * 3. Tracks allocation statistics for optimization
 */
public class PriorityAwareAllocationStrategy implements NpuAllocationStrategy {
    
    private int totalAllocations = 0;
    private int largeTaskAllocations = 0;
    private int smallTaskAllocations = 0;
    
    // Threshold for considering a task "large"
    private static final int LARGE_TASK_THRESHOLD = 3;
    private static final double HIGH_RESOURCE_THRESHOLD = 0.6;
    
    @Override
    public Map<String, List<String>> allocateNpus(List<NpuTask> waitingTasks, List<Npu> availableNpus) {
        Map<String, List<String>> allocations = new HashMap<>();
        
        // Sort tasks by priority (larger tasks first, then by resource demand, then by arrival time)
        List<NpuTask> prioritizedTasks = waitingTasks.stream()
                .sorted(this::compareTaskPriority)
                .collect(Collectors.toList());
        
        for (NpuTask task : prioritizedTasks) {
            List<String> selectedNpuIds = selectOptimalNpus(task, availableNpus);
            
            if (selectedNpuIds.size() >= task.getNpuDemand()) {
                allocations.put(task.getId(), selectedNpuIds.subList(0, task.getNpuDemand()));
            }
        }
        
        return allocations;
    }
    
    /**
     * Compares tasks for priority ordering.
     * Higher priority tasks are allocated first.
     */
    private int compareTaskPriority(NpuTask t1, NpuTask t2) {
        // First, prioritize by task size (larger tasks first)
        int sizeComparison = Integer.compare(t2.getNpuDemand(), t1.getNpuDemand());
        if (sizeComparison != 0) {
            return sizeComparison;
        }
        
        // Then by resource intensity (higher resource demand first)
        double t1ResourceIntensity = t1.getNpuTimeSliceRatio() + t1.getHbmDemand();
        double t2ResourceIntensity = t2.getNpuTimeSliceRatio() + t2.getHbmDemand();
        int resourceComparison = Double.compare(t2ResourceIntensity, t1ResourceIntensity);
        if (resourceComparison != 0) {
            return resourceComparison;
        }
        
        // Finally by arrival time (FIFO for same priority)
        return t1.getArrivalTime().compareTo(t2.getArrivalTime());
    }
    
    /**
     * Selects optimal NPUs based on task characteristics.
     */
    private List<String> selectOptimalNpus(NpuTask task, List<Npu> availableNpus) {
        boolean isLargeTask = task.getNpuDemand() >= LARGE_TASK_THRESHOLD;
        boolean isHighResourceTask = (task.getNpuTimeSliceRatio() + task.getHbmDemand()) > HIGH_RESOURCE_THRESHOLD;
        
        if (isLargeTask || isHighResourceTask) {
            // For large/high-resource tasks, use least-loaded strategy to spread load
            return selectLeastLoadedNpus(task, availableNpus);
        } else {
            // For small tasks, use best-fit to minimize fragmentation
            return selectBestFitNpus(task, availableNpus);
        }
    }
    
    /**
     * Selects least loaded NPUs (for large tasks).
     */
    private List<String> selectLeastLoadedNpus(NpuTask task, List<Npu> availableNpus) {
        List<Npu> candidateNpus = availableNpus.stream()
                .filter(npu -> npu.canAccommodateTask(task.getNpuTimeSliceRatio(), task.getHbmDemand()))
                .sorted(Comparator.comparingDouble(Npu::getUtilizationScore))
                .collect(Collectors.toList());
        
        return candidateNpus.stream()
                .limit(task.getNpuDemand())
                .map(Npu::getId)
                .collect(Collectors.toList());
    }
    
    /**
     * Selects best fit NPUs (for small tasks).
     */
    private List<String> selectBestFitNpus(NpuTask task, List<Npu> availableNpus) {
        List<Npu> candidateNpus = availableNpus.stream()
                .filter(npu -> npu.canAccommodateTask(task.getNpuTimeSliceRatio(), task.getHbmDemand()))
                .sorted((n1, n2) -> Double.compare(n2.getUtilizationScore(), n1.getUtilizationScore()))
                .collect(Collectors.toList());
        
        return candidateNpus.stream()
                .limit(task.getNpuDemand())
                .map(Npu::getId)
                .collect(Collectors.toList());
    }
    
    @Override
    public void onAllocationComplete(int allocatedTasks, int remainingTasks) {
        totalAllocations += allocatedTasks;
        
        // This could be enhanced to track more detailed statistics
        if (allocatedTasks > 0) {
            System.out.println("    [Priority Strategy] Allocated " + allocatedTasks + 
                             " tasks, " + remainingTasks + " remaining");
        }
    }
    
    @Override
    public void initialize() {
        totalAllocations = 0;
        largeTaskAllocations = 0;
        smallTaskAllocations = 0;
    }
    
    @Override
    public String getStrategyName() {
        return "Priority-Aware (Hybrid)";
    }
    
    /**
     * Gets allocation statistics.
     */
    public String getStatistics() {
        return String.format("Total allocations: %d, Large tasks: %d, Small tasks: %d", 
                           totalAllocations, largeTaskAllocations, smallTaskAllocations);
    }
}
