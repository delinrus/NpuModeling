package ru.ve.npu.sim;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks various statistics during NPU load balancing simulation.
 */
@Getter
@Setter
public class SimulationStatistics {
    
    private int totalTasks;
    private int acceptedTasks;
    private int completedTasks;
    private int processedEvents;
    
    private List<SimTime> responseTimes;
    private long simulationStartTime;
    private long simulationEndTime;
    
    public SimulationStatistics() {
        this.totalTasks = 0;
        this.acceptedTasks = 0;
        this.completedTasks = 0;
        this.processedEvents = 0;
        this.responseTimes = new ArrayList<>();
        this.simulationStartTime = 0;
        this.simulationEndTime = 0;
    }
    
    public void incrementTotalTasks() {
        totalTasks++;
    }
    
    public void incrementAcceptedTasks() {
        acceptedTasks++;
    }
    
    
    public void incrementCompletedTasks() {
        completedTasks++;
    }
    
    public void incrementProcessedEvents() {
        processedEvents++;
    }
    
    public void addResponseTime(SimTime responseTime) {
        responseTimes.add(responseTime);
    }
    
    /**
     * Calculates the task acceptance rate.
     * @return acceptance rate (0.0 to 1.0)
     */
    public double getAcceptanceRate() {
        return totalTasks > 0 ? (double) acceptedTasks / totalTasks : 0.0;
    }
    
    
    /**
     * Calculates the average response time.
     * @return average response time, or SimTime.ZERO if no completed tasks
     */
    public SimTime getAverageResponseTime() {
        if (responseTimes.isEmpty()) {
            return SimTime.ZERO;
        }
        long avgNanos = (long) responseTimes.stream()
                .mapToLong(SimTime::toNanos)
                .average()
                .orElse(0.0);
        return SimTime.ofNanos(avgNanos);
    }
    
    /**
     * Gets the minimum response time.
     * @return minimum response time, or SimTime.ZERO if no completed tasks
     */
    public SimTime getMinResponseTime() {
        return responseTimes.isEmpty() ? SimTime.ZERO : 
               responseTimes.stream().min(SimTime::compareTo).orElse(SimTime.ZERO);
    }
    
    /**
     * Gets the maximum response time.
     * @return maximum response time, or SimTime.ZERO if no completed tasks
     */
    public SimTime getMaxResponseTime() {
        return responseTimes.isEmpty() ? SimTime.ZERO : 
               responseTimes.stream().max(SimTime::compareTo).orElse(SimTime.ZERO);
    }
    
    /**
     * Gets the simulation duration in milliseconds.
     * @return duration in milliseconds
     */
    public long getSimulationDurationMs() {
        return simulationEndTime > simulationStartTime ? 
               simulationEndTime - simulationStartTime : 0;
    }
    
    /**
     * Calculates the throughput (completed tasks per time unit).
     * @param simulationTime the total simulation time
     * @return throughput, or 0.0 if simulation time is 0
     */
    public double getThroughput(SimTime simulationTime) {
        return simulationTime.isPositive() ? completedTasks / simulationTime.toSecondsDouble() : 0.0;
    }
    
    /**
     * Creates a copy of these statistics.
     * @return a new SimulationStatistics object with the same values
     */
    public SimulationStatistics copy() {
        SimulationStatistics copy = new SimulationStatistics();
        copy.totalTasks = this.totalTasks;
        copy.acceptedTasks = this.acceptedTasks;
        copy.completedTasks = this.completedTasks;
        copy.processedEvents = this.processedEvents;
        copy.responseTimes = new ArrayList<>(this.responseTimes);
        copy.simulationStartTime = this.simulationStartTime;
        copy.simulationEndTime = this.simulationEndTime;
        return copy;
    }
    
    /**
     * Resets all statistics to initial values.
     */
    public void reset() {
        totalTasks = 0;
        acceptedTasks = 0;
        completedTasks = 0;
        processedEvents = 0;
        responseTimes.clear();
        simulationStartTime = 0;
        simulationEndTime = 0;
    }
    
    @Override
    public String toString() {
        return "SimulationStatistics{" +
                "totalTasks=" + totalTasks +
                ", acceptedTasks=" + acceptedTasks +
                ", completedTasks=" + completedTasks +
                ", acceptanceRate=" + String.format("%.3f", getAcceptanceRate()) +
                ", avgResponseTime=" + getAverageResponseTime() +
                ", processedEvents=" + processedEvents +
                '}';
    }
}

