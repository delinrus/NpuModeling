package ru.ve.npu.sim;

/**
 * Interface for task simulation context.
 * Allows events to interact with the simulation without tight coupling.
 */
public interface TaskSimulationContext {
    
    /**
     * Processes a task arrival event.
     * @param event the task arrival event
     */
    void processTaskArrival(TaskArrivalEvent event);
    
    /**
     * Processes a task completion event.
     * @param event the task completion event
     */
    void processTaskCompletion(TaskCompletionEvent event);
}

