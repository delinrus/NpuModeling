package ru.ve.npu.sim.strategies;

import ru.ve.npu.sim.Npu;
import ru.ve.npu.sim.NpuAllocationStrategy;
import ru.ve.npu.sim.NpuTask;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Least-loaded allocation strategy.
 * Assigns tasks to NPUs with the lowest current utilization to balance load evenly.
 */
public class LeastLoadedAllocationStrategy implements NpuAllocationStrategy {
    
    @Override
    public Map<String, List<String>> allocateNpus(List<NpuTask> waitingTasks, List<Npu> availableNpus) {
        Map<String, List<String>> allocations = new HashMap<>();
        
        // Sort tasks by arrival time (FIFO)
        List<NpuTask> sortedTasks = new ArrayList<>(waitingTasks);
        sortedTasks.sort(Comparator.comparing(NpuTask::getArrivalTime));
        
        for (NpuTask task : sortedTasks) {
            List<String> selectedNpuIds = selectLeastLoadedNpus(task, availableNpus);
            
            if (selectedNpuIds.size() >= task.getNpuDemand()) {
                allocations.put(task.getId(), selectedNpuIds.subList(0, task.getNpuDemand()));
            }
        }
        
        return allocations;
    }
    
    /**
     * Selects NPUs with the lowest utilization that can accommodate the task.
     * This spreads load evenly across all NPUs.
     */
    private List<String> selectLeastLoadedNpus(NpuTask task, List<Npu> availableNpus) {
        List<Npu> candidateNpus = availableNpus.stream()
                .filter(npu -> npu.canAccommodateTask(task.getNpuTimeSliceRatio(), task.getHbmDemand()))
                .sorted(Comparator.comparingDouble(Npu::getUtilizationScore)) // Lowest utilization first
                .collect(Collectors.toList());
        
        return candidateNpus.stream()
                .limit(task.getNpuDemand())
                .map(Npu::getId)
                .collect(Collectors.toList());
    }
    
    @Override
    public String getStrategyName() {
        return "Least-Loaded";
    }
}
