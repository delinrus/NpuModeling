package ru.ve.npu.sim.strategies;

import ru.ve.npu.sim.Npu;
import ru.ve.npu.sim.NpuAllocationStrategy;
import ru.ve.npu.sim.NpuTask;

import java.util.*;

/**
 * First-fit allocation strategy.
 * Assigns tasks to the first available NPUs in order.
 */
public class FirstFitAllocationStrategy implements NpuAllocationStrategy {
    
    @Override
    public Map<String, List<String>> allocateNpus(List<NpuTask> waitingTasks, List<Npu> availableNpus) {
        Map<String, List<String>> allocations = new HashMap<>();
        
        // Sort tasks by arrival time (FIFO)
        List<NpuTask> sortedTasks = new ArrayList<>(waitingTasks);
        sortedTasks.sort(Comparator.comparing(NpuTask::getArrivalTime));
        
        for (NpuTask task : sortedTasks) {
            List<String> selectedNpuIds = selectFirstFitNpus(task, availableNpus);
            
            if (selectedNpuIds.size() >= task.getNpuDemand()) {
                allocations.put(task.getId(), selectedNpuIds.subList(0, task.getNpuDemand()));
            }
        }
        
        return allocations;
    }
    
    /**
     * Selects the first available NPUs that can accommodate the task.
     */
    private List<String> selectFirstFitNpus(NpuTask task, List<Npu> availableNpus) {
        List<String> selectedNpuIds = new ArrayList<>();
        
        for (Npu npu : availableNpus) {
            if (npu.canAccommodateTask(task.getNpuTimeSliceRatio(), task.getHbmDemand())) {
                selectedNpuIds.add(npu.getId());
                
                if (selectedNpuIds.size() >= task.getNpuDemand()) {
                    break;
                }
            }
        }
        
        return selectedNpuIds;
    }
    
    @Override
    public String getStrategyName() {
        return "First-Fit";
    }
}
