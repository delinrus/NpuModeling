package ru.ve.npu.sim;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * Represents a task that should be load balanced among the NPU pool.
 * Contains all necessary parameters for NPU resource allocation and scheduling.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NpuTask {
    
    /**
     * Unique identifier for the task.
     */
    private String id;
    
    /**
     * Time when the task arrives in the system.
     */
    private SimTime arrivalTime;
    
    /**
     * The number of NPUs the task should be run on.
     */
    private int npuDemand;
    
    /**
     * The percent of the NPU compute power the task takes on each involved NPU.
     * Value should be between 0.0 and 1.0 (0% to 100%).
     */
    private double npuTimeSliceRatio;
    
    /**
     * The percent of used HBM on each involved NPU.
     * Value should be between 0.0 and 1.0 (0% to 100%).
     * Assumption: All NPUs are of the same type, so we can use percentage.
     */
    private double hbmDemand;
    
    /**
     * Time when the task is expected to complete.
     * This can be calculated based on task requirements and NPU allocation.
     */
    private SimTime taskCompletionTime;
    
    /**
     * Calculates the duration of the task.
     * @return the duration from arrival to completion
     */
    public SimTime getDuration() {
        return taskCompletionTime.minus(arrivalTime);
    }
    
    /**
     * Checks if the task resource demands are valid.
     * @return true if all resource demands are within valid ranges
     */
    public boolean isValidTask() {
        return npuDemand > 0 
            && npuTimeSliceRatio >= 0.0 && npuTimeSliceRatio <= 1.0
            && hbmDemand >= 0.0 && hbmDemand <= 1.0
            && taskCompletionTime.isAfterOrEqual(arrivalTime);
    }
    
    /**
     * Calculates the total NPU compute resources required by this task.
     * @return total compute units (npuDemand * npuTimeSliceRatio)
     */
    public double getTotalComputeUnits() {
        return npuDemand * npuTimeSliceRatio;
    }
    
    /**
     * Calculates the total HBM resources required by this task.
     * @return total HBM units (npuDemand * hbmDemand)
     */
    public double getTotalHbmUnits() {
        return npuDemand * hbmDemand;
    }
    
    @Override
    public String toString() {
        return "NpuTask{" +
                "id='" + id + '\'' +
                ", arrivalTime=" + arrivalTime +
                ", npuDemand=" + npuDemand +
                ", npuTimeSliceRatio=" + npuTimeSliceRatio +
                ", hbmDemand=" + hbmDemand +
                ", taskCompletionTime=" + taskCompletionTime +
                ", duration=" + getDuration() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        NpuTask npuTask = (NpuTask) o;
        return id != null ? id.equals(npuTask.id) : npuTask.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

