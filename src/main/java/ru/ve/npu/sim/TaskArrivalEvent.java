package ru.ve.npu.sim;

import lombok.Getter;

/**
 * Event representing the arrival of a new task in the NPU simulation.
 * This event triggers the load balancing logic to allocate NPUs for the task.
 */
@Getter
public class TaskArrivalEvent extends NpuSimEvent {
    
    private final NpuTask task;
    
    public TaskArrivalEvent(NpuTask task) {
        super(task.getArrivalTime(), Type.ARRIVAL);
        this.task = task;
    }
    
    @Override
    public void process(Object context) {
        if (!(context instanceof TaskSimulationContext)) {
            throw new IllegalArgumentException("Context must implement TaskSimulationContext");
        }
        
        TaskSimulationContext simulation = (TaskSimulationContext) context;
        simulation.processTaskArrival(this);
    }
    
    @Override
    public String toString() {
        return "TaskArrivalEvent{" +
                "time=" + getTime() +
                ", task=" + task.getId() +
                ", npuDemand=" + task.getNpuDemand() +
                ", seq=" + getSeq() +
                '}';
    }
}
