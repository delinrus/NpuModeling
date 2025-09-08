package ru.ve.npu.sim;

import lombok.Getter;
import java.util.List;

/**
 * Event representing the completion of a task in the NPU simulation.
 * This event triggers the deallocation of NPUs that were assigned to the task.
 */
@Getter
public class TaskCompletionEvent extends NpuSimEvent {
    
    private final NpuTask task;
    private final List<String> allocatedNpuIds;
    
    public TaskCompletionEvent(NpuTask task, List<String> allocatedNpuIds) {
        super(task.getTaskCompletionTime(), Type.COMPLETION);
        this.task = task;
        this.allocatedNpuIds = allocatedNpuIds;
    }
    
    @Override
    public void process(Object context) {
        if (!(context instanceof TaskSimulationContext)) {
            throw new IllegalArgumentException("Context must implement TaskSimulationContext");
        }
        
        TaskSimulationContext simulation = (TaskSimulationContext) context;
        simulation.processTaskCompletion(this);
    }
    
    @Override
    public String toString() {
        return "TaskCompletionEvent{" +
                "time=" + getTime() +
                ", task=" + task.getId() +
                ", allocatedNpus=" + allocatedNpuIds +
                ", seq=" + getSeq() +
                '}';
    }
}
