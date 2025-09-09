package ru.ve.npu.sim;

import java.util.List;
import java.util.Map;

/**
 * Interface for NPU allocation strategies.
 * Implementations define how tasks should be allocated to NPUs based on current system state.
 */
public interface NpuAllocationStrategy {
    
    /**
     * Allocates NPUs to waiting tasks based on current system state.
     * 
     * @param waitingTasks list of tasks waiting for NPU allocation
     * @param availableNpus list of all NPUs in the pool with their current state
     * @return mapping from task ID to list of NPU IDs that should be allocated to that task.
     *         Only returns mappings for tasks that can be allocated. Tasks not in the map
     *         remain in the waiting queue.
     */
    Map<String, List<String>> allocateNpus(List<NpuTask> waitingTasks, List<Npu> availableNpus);
    
    /**
     * Returns the name of this allocation strategy for logging and debugging.
     * 
     * @return strategy name
     */
    String getStrategyName();
    
    /**
     * Optional method for strategy-specific configuration or state reset.
     * Called when the strategy is first set or when simulation is reset.
     */
    default void initialize() {
        // Default implementation does nothing
    }
    
    /**
     * Optional method called after each allocation round to allow strategy
     * to update internal state or collect metrics.
     * 
     * @param allocatedTasks number of tasks allocated in this round
     * @param remainingTasks number of tasks still waiting
     */
    default void onAllocationComplete(int allocatedTasks, int remainingTasks) {
        // Default implementation does nothing
    }
}
