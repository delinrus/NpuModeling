package ru.ve.npu.sim;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages a pool of NPUs for load balancing tasks.
 * Provides various load balancing strategies.
 */
@Getter
public class NpuPool {
    
    /**
     * Load balancing strategy enumeration.
     */
    public enum LoadBalancingStrategy {
        FIRST_FIT,      // Assign to first available NPUs
        BEST_FIT,       // Assign to NPUs with least available capacity that can fit
        ROUND_ROBIN,    // Assign in round-robin fashion
        LEAST_LOADED    // Assign to least loaded NPUs
    }
    
    private final List<Npu> npus;
    private final LoadBalancingStrategy strategy;
    private int roundRobinIndex;
    
    public NpuPool(int npuCount, LoadBalancingStrategy strategy) {
        this.strategy = strategy;
        this.npus = new ArrayList<>();
        this.roundRobinIndex = 0;
        
        // Initialize NPUs
        for (int i = 0; i < npuCount; i++) {
            npus.add(new Npu("NPU-" + i));
        }
    }
    
    /**
     * Attempts to allocate NPUs for a task.
     * @param task the task to allocate NPUs for
     * @return list of allocated NPU IDs, or empty list if allocation failed
     */
    public List<String> allocateNpusForTask(NpuTask task) {
        if (task == null || !task.isValidTask()) {
            return Collections.emptyList();
        }
        
        List<Npu> selectedNpus = selectNpusForTask(task);
        
        if (selectedNpus.size() < task.getNpuDemand()) {
            return Collections.emptyList(); // Not enough NPUs available
        }
        
        // Allocate resources on selected NPUs
        List<String> allocatedNpuIds = new ArrayList<>();
        try {
            for (Npu npu : selectedNpus) {
                npu.allocateTask(task.getId(), task.getNpuTimeSliceRatio(), task.getHbmDemand());
                allocatedNpuIds.add(npu.getId());
            }
        } catch (IllegalStateException e) {
            // Rollback allocations if any fail
            rollbackAllocations(task, allocatedNpuIds);
            return Collections.emptyList();
        }
        
        return allocatedNpuIds;
    }
    
    /**
     * Deallocates NPUs for a completed task.
     * @param task the completed task
     * @param allocatedNpuIds the NPU IDs that were allocated to this task
     */
    public void deallocateNpusForTask(NpuTask task, List<String> allocatedNpuIds) {
        for (String npuId : allocatedNpuIds) {
            Npu npu = findNpuById(npuId);
            if (npu != null) {
                npu.deallocateTask(task.getId(), task.getNpuTimeSliceRatio(), task.getHbmDemand());
            }
        }
    }
    
    /**
     * Selects NPUs for a task based on the load balancing strategy.
     */
    private List<Npu> selectNpusForTask(NpuTask task) {
        switch (strategy) {
            case FIRST_FIT:
                return selectFirstFit(task);
            case BEST_FIT:
                return selectBestFit(task);
            case ROUND_ROBIN:
                return selectRoundRobin(task);
            case LEAST_LOADED:
                return selectLeastLoaded(task);
            default:
                return selectFirstFit(task);
        }
    }
    
    /**
     * First-fit strategy: select first available NPUs.
     */
    private List<Npu> selectFirstFit(NpuTask task) {
        List<Npu> selected = new ArrayList<>();
        for (Npu npu : npus) {
            if (npu.canAccommodateTask(task.getNpuTimeSliceRatio(), task.getHbmDemand())) {
                selected.add(npu);
                if (selected.size() >= task.getNpuDemand()) {
                    break;
                }
            }
        }
        return selected;
    }
    
    /**
     * Best-fit strategy: select NPUs with least available capacity that can fit.
     */
    private List<Npu> selectBestFit(NpuTask task) {
        List<Npu> availableNpus = npus.stream()
                .filter(npu -> npu.canAccommodateTask(task.getNpuTimeSliceRatio(), task.getHbmDemand()))
                .sorted((n1, n2) -> Double.compare(n2.getUtilizationScore(), n1.getUtilizationScore()))
                .collect(Collectors.toList());
        
        return availableNpus.stream()
                .limit(task.getNpuDemand())
                .collect(Collectors.toList());
    }
    
    /**
     * Round-robin strategy: select NPUs in round-robin fashion.
     */
    private List<Npu> selectRoundRobin(NpuTask task) {
        List<Npu> selected = new ArrayList<>();
        int attempts = 0;
        int maxAttempts = npus.size() * 2; // Prevent infinite loop
        
        while (selected.size() < task.getNpuDemand() && attempts < maxAttempts) {
            Npu npu = npus.get(roundRobinIndex);
            roundRobinIndex = (roundRobinIndex + 1) % npus.size();
            
            if (npu.canAccommodateTask(task.getNpuTimeSliceRatio(), task.getHbmDemand()) 
                && !selected.contains(npu)) {
                selected.add(npu);
            }
            attempts++;
        }
        
        return selected;
    }
    
    /**
     * Least-loaded strategy: select NPUs with lowest utilization.
     */
    private List<Npu> selectLeastLoaded(NpuTask task) {
        List<Npu> availableNpus = npus.stream()
                .filter(npu -> npu.canAccommodateTask(task.getNpuTimeSliceRatio(), task.getHbmDemand()))
                .sorted((n1, n2) -> Double.compare(n1.getUtilizationScore(), n2.getUtilizationScore()))
                .collect(Collectors.toList());
        
        return availableNpus.stream()
                .limit(task.getNpuDemand())
                .collect(Collectors.toList());
    }
    
    /**
     * Rolls back allocations in case of failure.
     */
    private void rollbackAllocations(NpuTask task, List<String> allocatedNpuIds) {
        for (String npuId : allocatedNpuIds) {
            Npu npu = findNpuById(npuId);
            if (npu != null) {
                try {
                    npu.deallocateTask(task.getId(), task.getNpuTimeSliceRatio(), task.getHbmDemand());
                } catch (Exception e) {
                    // Log error but continue rollback
                    System.err.println("Error during rollback for NPU " + npuId + ": " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Finds an NPU by its ID.
     */
    private Npu findNpuById(String npuId) {
        return npus.stream()
                .filter(npu -> npu.getId().equals(npuId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Gets the current pool utilization statistics.
     */
    public PoolStatistics getStatistics() {
        double avgComputeUtil = npus.stream()
                .mapToDouble(Npu::getCurrentComputeUtilization)
                .average()
                .orElse(0.0);
        
        double avgHbmUtil = npus.stream()
                .mapToDouble(Npu::getCurrentHbmUtilization)
                .average()
                .orElse(0.0);
        
        long idleNpus = npus.stream()
                .mapToLong(npu -> npu.isIdle() ? 1 : 0)
                .sum();
        
        int totalRunningTasks = npus.stream()
                .mapToInt(npu -> npu.getRunningTasks().size())
                .sum();
        
        return new PoolStatistics(avgComputeUtil, avgHbmUtil, idleNpus, totalRunningTasks);
    }
    
    /**
     * Pool statistics data class.
     */
    public static class PoolStatistics {
        @Getter private final double averageComputeUtilization;
        @Getter private final double averageHbmUtilization;
        @Getter private final long idleNpuCount;
        @Getter private final int totalRunningTasks;
        
        public PoolStatistics(double avgComputeUtil, double avgHbmUtil, long idleNpus, int runningTasks) {
            this.averageComputeUtilization = avgComputeUtil;
            this.averageHbmUtilization = avgHbmUtil;
            this.idleNpuCount = idleNpus;
            this.totalRunningTasks = runningTasks;
        }
        
        @Override
        public String toString() {
            return "PoolStatistics{" +
                    "avgComputeUtil=" + String.format("%.3f", averageComputeUtilization) +
                    ", avgHbmUtil=" + String.format("%.3f", averageHbmUtilization) +
                    ", idleNpus=" + idleNpuCount +
                    ", runningTasks=" + totalRunningTasks +
                    '}';
        }
    }
    
    @Override
    public String toString() {
        return "NpuPool{" +
                "npuCount=" + npus.size() +
                ", strategy=" + strategy +
                ", statistics=" + getStatistics() +
                '}';
    }
}

