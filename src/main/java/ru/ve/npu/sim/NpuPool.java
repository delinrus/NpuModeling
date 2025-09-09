package ru.ve.npu.sim;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages a pool of NPUs for load balancing tasks.
 * Uses external allocation strategies for flexible task assignment.
 */
@Getter
public class NpuPool {
    
    private final List<Npu> npus;
    private NpuAllocationStrategy allocationStrategy;
    
    public NpuPool(int npuCount, NpuAllocationStrategy allocationStrategy) {
        this.allocationStrategy = allocationStrategy;
        this.npus = new ArrayList<>();
        
        // Initialize NPUs
        for (int i = 0; i < npuCount; i++) {
            npus.add(new Npu("NPU-" + i));
        }
        
        // Initialize strategy
        if (this.allocationStrategy != null) {
            this.allocationStrategy.initialize();
        }
    }
    
    /**
     * Sets a new allocation strategy.
     * @param strategy the new allocation strategy
     */
    public void setAllocationStrategy(NpuAllocationStrategy strategy) {
        this.allocationStrategy = strategy;
        if (strategy != null) {
            strategy.initialize();
        }
    }
    
    /**
     * Attempts to allocate NPUs for multiple waiting tasks using the allocation strategy.
     * @param waitingTasks list of tasks waiting for allocation
     * @return mapping from task ID to list of allocated NPU IDs
     */
    public Map<String, List<String>> allocateNpusForTasks(List<NpuTask> waitingTasks) {
        if (waitingTasks == null || waitingTasks.isEmpty() || allocationStrategy == null) {
            return Collections.emptyMap();
        }
        
        // Filter valid tasks
        List<NpuTask> validTasks = waitingTasks.stream()
                .filter(task -> task != null && task.isValidTask())
                .collect(Collectors.toList());
        
        if (validTasks.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // Get allocation decisions from strategy
        Map<String, List<String>> allocationDecisions = allocationStrategy.allocateNpus(validTasks, npus);
        
        // Process allocations and rollback on failure
        Map<String, List<String>> successfulAllocations = new HashMap<>();
        List<String> failedAllocations = new ArrayList<>();
        
        for (Map.Entry<String, List<String>> entry : allocationDecisions.entrySet()) {
            String taskId = entry.getKey();
            List<String> npuIds = entry.getValue();
            
            // Find the task
            NpuTask task = validTasks.stream()
                    .filter(t -> t.getId().equals(taskId))
                    .findFirst()
                    .orElse(null);
            
            if (task != null) {
                // Try to allocate resources
                if (tryAllocateTask(task, npuIds)) {
                    successfulAllocations.put(taskId, npuIds);
                } else {
                    failedAllocations.add(taskId);
                }
            }
        }
        
        // Notify strategy about allocation results
        allocationStrategy.onAllocationComplete(
            successfulAllocations.size(), 
            validTasks.size() - successfulAllocations.size()
        );
        
        return successfulAllocations;
    }
    
    /**
     * Tries to allocate a task to specific NPUs.
     * @param task the task to allocate
     * @param npuIds the NPU IDs to allocate to
     * @return true if allocation successful, false otherwise
     */
    private boolean tryAllocateTask(NpuTask task, List<String> npuIds) {
        List<Npu> selectedNpus = new ArrayList<>();
        
        // Find NPUs by ID
        for (String npuId : npuIds) {
            Npu npu = findNpuById(npuId);
            if (npu == null) {
                return false; // NPU not found
            }
            selectedNpus.add(npu);
        }
        
        // Check if all NPUs can accommodate the task
        for (Npu npu : selectedNpus) {
            if (!npu.canAccommodateTask(task.getNpuTimeSliceRatio(), task.getHbmDemand())) {
                return false; // Not enough resources
            }
        }
        
        // Allocate resources on all NPUs
        List<String> allocatedNpuIds = new ArrayList<>();
        try {
            for (Npu npu : selectedNpus) {
                npu.allocateTask(task.getId(), task.getNpuTimeSliceRatio(), task.getHbmDemand());
                allocatedNpuIds.add(npu.getId());
            }
            return true;
        } catch (IllegalStateException e) {
            // Rollback allocations if any fail
            rollbackAllocations(task, allocatedNpuIds);
            return false;
        }
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
                ", strategy=" + (allocationStrategy != null ? allocationStrategy.getStrategyName() : "null") +
                ", statistics=" + getStatistics() +
                '}';
    }
}

