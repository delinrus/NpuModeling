package ru.ve.npu.sim;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a single NPU in the pool with its current resource utilization.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Npu {
    
    /**
     * Unique identifier for the NPU.
     */
    private String id;
    
    /**
     * Current compute utilization (0.0 to 1.0).
     */
    private double currentComputeUtilization;
    
    /**
     * Current HBM utilization (0.0 to 1.0).
     */
    private double currentHbmUtilization;
    
    /**
     * Set of currently running tasks on this NPU.
     */
    private Set<String> runningTasks;
    
    public Npu(String id) {
        this.id = id;
        this.currentComputeUtilization = 0.0;
        this.currentHbmUtilization = 0.0;
        this.runningTasks = new HashSet<>();
    }
    
    /**
     * Checks if the NPU can accommodate a new task.
     * @param computeRatio required compute ratio for the task
     * @param hbmRatio required HBM ratio for the task
     * @return true if the NPU has enough available resources
     */
    public boolean canAccommodateTask(double computeRatio, double hbmRatio) {
        return (currentComputeUtilization + computeRatio) <= 1.0 
            && (currentHbmUtilization + hbmRatio) <= 1.0;
    }
    
    /**
     * Allocates resources for a task on this NPU.
     * @param taskId the task identifier
     * @param computeRatio required compute ratio
     * @param hbmRatio required HBM ratio
     * @throws IllegalStateException if resources are not available
     */
    public void allocateTask(String taskId, double computeRatio, double hbmRatio) {
        if (!canAccommodateTask(computeRatio, hbmRatio)) {
            throw new IllegalStateException("NPU " + id + " cannot accommodate task " + taskId);
        }
        
        currentComputeUtilization += computeRatio;
        currentHbmUtilization += hbmRatio;
        runningTasks.add(taskId);
    }
    
    /**
     * Deallocates resources for a completed task.
     * @param taskId the task identifier
     * @param computeRatio compute ratio to free
     * @param hbmRatio HBM ratio to free
     */
    public void deallocateTask(String taskId, double computeRatio, double hbmRatio) {
        if (!runningTasks.contains(taskId)) {
            throw new IllegalStateException("Task " + taskId + " is not running on NPU " + id);
        }
        
        currentComputeUtilization = Math.max(0.0, currentComputeUtilization - computeRatio);
        currentHbmUtilization = Math.max(0.0, currentHbmUtilization - hbmRatio);
        runningTasks.remove(taskId);
    }
    
    /**
     * Gets the available compute capacity.
     * @return available compute ratio (0.0 to 1.0)
     */
    public double getAvailableComputeCapacity() {
        return Math.max(0.0, 1.0 - currentComputeUtilization);
    }
    
    /**
     * Gets the available HBM capacity.
     * @return available HBM ratio (0.0 to 1.0)
     */
    public double getAvailableHbmCapacity() {
        return Math.max(0.0, 1.0 - currentHbmUtilization);
    }
    
    /**
     * Checks if the NPU is idle (no running tasks).
     * @return true if no tasks are running
     */
    public boolean isIdle() {
        return runningTasks.isEmpty();
    }
    
    /**
     * Gets the utilization score (max of compute and HBM utilization).
     * @return utilization score (0.0 to 1.0)
     */
    public double getUtilizationScore() {
        return Math.max(currentComputeUtilization, currentHbmUtilization);
    }
    
    @Override
    public String toString() {
        return "Npu{" +
                "id='" + id + '\'' +
                ", computeUtil=" + String.format("%.2f", currentComputeUtilization) +
                ", hbmUtil=" + String.format("%.2f", currentHbmUtilization) +
                ", runningTasks=" + runningTasks.size() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Npu npu = (Npu) o;
        return id != null ? id.equals(npu.id) : npu.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

