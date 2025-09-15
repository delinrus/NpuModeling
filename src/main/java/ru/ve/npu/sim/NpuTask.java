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
     * Estimated processing time required to execute the task (service time).
     * This is a duration, not an absolute timestamp.
     */
    private SimTime processingTimeEstimate;
    
    /**
     * Returns the estimated processing duration for the task (service time).
     */
    public SimTime getDuration() {
        return processingTimeEstimate;
    }
    
    /**
     * Checks if the task resource demands are valid.
     * @return true if all resource demands are within valid ranges
     */
    public boolean isValidTask() {
        return npuDemand > 0 
            && npuTimeSliceRatio >= 0.0 && npuTimeSliceRatio <= 1.0
            && hbmDemand >= 0.0 && hbmDemand <= 1.0
            && processingTimeEstimate != null && processingTimeEstimate.isPositive();
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
                ", processingTimeEstimate=" + processingTimeEstimate +
                ", duration=" + processingTimeEstimate +
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

