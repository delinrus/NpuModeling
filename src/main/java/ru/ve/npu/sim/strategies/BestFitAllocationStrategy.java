package ru.ve.npu.sim.strategies;

import ru.ve.npu.sim.Npu;
import ru.ve.npu.sim.NpuAllocationStrategy;
import ru.ve.npu.sim.NpuTask;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Best-fit allocation strategy.
 * Assigns tasks to NPUs with the least available capacity that can still fit the task.
 * This minimizes fragmentation by filling up NPUs more completely.
 */
public class BestFitAllocationStrategy implements NpuAllocationStrategy {
    
    @Override
    public Map<String, List<String>> allocateNpus(List<NpuTask> waitingTasks, List<Npu> availableNpus) {
        Map<String, List<String>> allocations = new HashMap<>();
        
        // Sort tasks by arrival time (FIFO)
        List<NpuTask> sortedTasks = new ArrayList<>(waitingTasks);
        sortedTasks.sort(Comparator.comparing(NpuTask::getArrivalTime));
        
        for (NpuTask task : sortedTasks) {
            List<String> selectedNpuIds = selectBestFitNpus(task, availableNpus);
            
            if (selectedNpuIds.size() >= task.getNpuDemand()) {
                allocations.put(task.getId(), selectedNpuIds.subList(0, task.getNpuDemand()));
            }
        }
        
        return allocations;
    }
    
    /**
     * Selects NPUs with the highest utilization that can still accommodate the task.
     * This minimizes fragmentation by preferring more loaded NPUs.
     */
    private List<String> selectBestFitNpus(NpuTask task, List<Npu> availableNpus) {
        List<Npu> candidateNpus = availableNpus.stream()
                .filter(npu -> npu.canAccommodateTask(task.getNpuTimeSliceRatio(), task.getHbmDemand()))
                .sorted((n1, n2) -> Double.compare(n2.getUtilizationScore(), n1.getUtilizationScore())) // Highest utilization first
                .collect(Collectors.toList());
        
        return candidateNpus.stream()
                .limit(task.getNpuDemand())
                .map(Npu::getId)
                .collect(Collectors.toList());
    }
    
    @Override
    public String getStrategyName() {
        return "Best-Fit";
    }
}
