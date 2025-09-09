package ru.ve.npu.sim.strategies;

import ru.ve.npu.sim.Npu;
import ru.ve.npu.sim.NpuAllocationStrategy;
import ru.ve.npu.sim.NpuTask;

import java.util.*;

/**
 * Round-robin allocation strategy.
 * Assigns tasks to NPUs in a circular fashion, maintaining fairness.
 */
public class RoundRobinAllocationStrategy implements NpuAllocationStrategy {
    
    private int roundRobinIndex = 0;
    
    @Override
    public Map<String, List<String>> allocateNpus(List<NpuTask> waitingTasks, List<Npu> availableNpus) {
        Map<String, List<String>> allocations = new HashMap<>();
        
        // Sort tasks by arrival time (FIFO)
        List<NpuTask> sortedTasks = new ArrayList<>(waitingTasks);
        sortedTasks.sort(Comparator.comparing(NpuTask::getArrivalTime));
        
        for (NpuTask task : sortedTasks) {
            List<String> selectedNpuIds = selectRoundRobinNpus(task, availableNpus);
            
            if (selectedNpuIds.size() >= task.getNpuDemand()) {
                allocations.put(task.getId(), selectedNpuIds.subList(0, task.getNpuDemand()));
            }
        }
        
        return allocations;
    }
    
    /**
     * Selects NPUs in round-robin fashion starting from the current index.
     */
    private List<String> selectRoundRobinNpus(NpuTask task, List<Npu> availableNpus) {
        List<String> selectedNpuIds = new ArrayList<>();
        
        if (availableNpus.isEmpty()) {
            return selectedNpuIds;
        }
        
        int attempts = 0;
        int maxAttempts = availableNpus.size() * 2; // Prevent infinite loop
        Set<String> alreadySelected = new HashSet<>();
        
        while (selectedNpuIds.size() < task.getNpuDemand() && attempts < maxAttempts) {
            Npu npu = availableNpus.get(roundRobinIndex);
            roundRobinIndex = (roundRobinIndex + 1) % availableNpus.size();
            
            // Check if NPU can accommodate task and hasn't been selected yet
            if (npu.canAccommodateTask(task.getNpuTimeSliceRatio(), task.getHbmDemand()) 
                && !alreadySelected.contains(npu.getId())) {
                selectedNpuIds.add(npu.getId());
                alreadySelected.add(npu.getId());
            }
            
            attempts++;
        }
        
        return selectedNpuIds;
    }
    
    @Override
    public void initialize() {
        roundRobinIndex = 0;
    }
    
    @Override
    public String getStrategyName() {
        return "Round-Robin";
    }
}
