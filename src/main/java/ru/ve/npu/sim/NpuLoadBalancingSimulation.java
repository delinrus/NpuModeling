package ru.ve.npu.sim;

import lombok.Getter;

import java.util.*;

/**
 * Main simulation class for NPU load balancing.
 * Manages the event-driven simulation of task arrivals and completions.
 */
@Getter
public class NpuLoadBalancingSimulation implements TaskSimulationContext {
    
    private final EventQueue eventQueue;
    private final NpuPool npuPool;
    private final SimulationStatistics statistics;
    private final Map<String, List<String>> taskNpuAllocations; // taskId -> allocated NPU IDs
    private final List<NpuTask> waitingTasks;
    private final List<NpuTask> completedTasks;
    
    private SimTime currentTime;
    private boolean running;
    
    public NpuLoadBalancingSimulation(int npuCount, NpuAllocationStrategy strategy) {
        this.eventQueue = new EventQueueImpl();
        this.npuPool = new NpuPool(npuCount, strategy);
        this.statistics = new SimulationStatistics();
        this.taskNpuAllocations = new HashMap<>();
        this.waitingTasks = new ArrayList<>();
        this.completedTasks = new ArrayList<>();
        this.currentTime = SimTime.ZERO;
        this.running = false;
    }
    
    /**
     * Adds a task to the simulation.
     * Creates a TaskArrivalEvent for the task.
     */
    public void addTask(NpuTask task) {
        if (task == null || !task.isValidTask()) {
            throw new IllegalArgumentException("Invalid task: " + task);
        }
        
        TaskArrivalEvent arrivalEvent = new TaskArrivalEvent(task);
        eventQueue.add(arrivalEvent);
        statistics.incrementTotalTasks();
    }
    
    /**
     * Runs the simulation until all events are processed.
     */
    public void runSimulation() {
        running = true;
        statistics.setSimulationStartTime(System.currentTimeMillis());
        
        System.out.println("Starting NPU Load Balancing Simulation...");
        System.out.println("NPU Pool: " + npuPool.getNpus().size() + " NPUs, Strategy: " + 
                          (npuPool.getAllocationStrategy() != null ? npuPool.getAllocationStrategy().getStrategyName() : "null"));
        System.out.println("Initial events in queue: " + eventQueue.size());
        
        while (!eventQueue.isEmpty() && running) {
            NpuSimEvent event = eventQueue.poll();
            currentTime = event.getTime();
            
            System.out.println("Processing event at time " + currentTime + ": " + event);
            event.process(this);
            
            // Update statistics
            statistics.incrementProcessedEvents();
            
            // Print periodic status updates
            if (statistics.getProcessedEvents() % 100 == 0) {
                printStatus();
            }
        }
        
        running = false;
        statistics.setSimulationEndTime(System.currentTimeMillis());
        
        System.out.println("Simulation completed.");
        printFinalStatistics();
    }
    
    /**
     * Stops the simulation.
     */
    public void stopSimulation() {
        running = false;
    }
    
    /**
     * Processes a task arrival event.
     */
    public void processTaskArrival(TaskArrivalEvent event) {
        NpuTask task = event.getTask();
        
        System.out.println("  Task " + task.getId() + " arrived (demands " + 
                          task.getNpuDemand() + " NPUs)");
        
        // Add task to waiting queue
        waitingTasks.add(task);
        
        // Try to allocate NPUs for waiting tasks
        processWaitingTasks();
    }
    
    /**
     * Processes a task completion event.
     */
    public void processTaskCompletion(TaskCompletionEvent event) {
        NpuTask task = event.getTask();
        List<String> allocatedNpuIds = event.getAllocatedNpuIds();
        
        System.out.println("  Task " + task.getId() + " completed");
        
        // Deallocate NPUs
        npuPool.deallocateNpusForTask(task, allocatedNpuIds);
        
        // Move task to completed list
        completedTasks.add(task);
        taskNpuAllocations.remove(task.getId());
        statistics.incrementCompletedTasks();
        
        // Update statistics
        SimTime responseTime = task.getTaskCompletionTime().minus(task.getArrivalTime());
        statistics.addResponseTime(responseTime);
        
        System.out.println("    Task " + task.getId() + " deallocated from NPUs: " + allocatedNpuIds);
        
        // Try to allocate NPUs for waiting tasks after deallocation
        processWaitingTasks();
    }
    
    /**
     * Processes waiting tasks by attempting to allocate NPUs using the allocation strategy.
     * Continues until no more tasks can be allocated.
     */
    private void processWaitingTasks() {
        if (waitingTasks.isEmpty()) {
            return;
        }
        
        boolean allocatedAny;
        do {
            allocatedAny = false;
            
            // Get allocation mapping from strategy
            Map<String, List<String>> allocations = npuPool.allocateNpusForTasks(waitingTasks);
            
            if (!allocations.isEmpty()) {
                allocatedAny = true;
                
                // Process each allocation
                for (Map.Entry<String, List<String>> entry : allocations.entrySet()) {
                    String taskId = entry.getKey();
                    List<String> allocatedNpuIds = entry.getValue();
                    
                    // Find the task
                    NpuTask task = waitingTasks.stream()
                            .filter(t -> t.getId().equals(taskId))
                            .findFirst()
                            .orElse(null);
                    
                    if (task != null) {
                        // Remove from waiting queue
                        waitingTasks.remove(task);
                        
                        // Store allocation
                        taskNpuAllocations.put(taskId, allocatedNpuIds);
                        statistics.incrementAcceptedTasks();
                        
                        // Schedule completion event
                        TaskCompletionEvent completionEvent = new TaskCompletionEvent(task, allocatedNpuIds);
                        eventQueue.add(completionEvent);
                        
                        System.out.println("    Task " + task.getId() + " ALLOCATED - NPUs: " + allocatedNpuIds);
                    }
                }
            }
        } while (allocatedAny && !waitingTasks.isEmpty());
        
        // Update statistics for remaining waiting tasks
        if (!waitingTasks.isEmpty()) {
            System.out.println("    " + waitingTasks.size() + " tasks remain in waiting queue");
        }
    }
    
    /**
     * Prints current simulation status.
     */
    private void printStatus() {
        System.out.println("--- Simulation Status (Time: " + currentTime + ") ---");
        System.out.println("Events processed: " + statistics.getProcessedEvents());
        System.out.println("Tasks: " + statistics.getAcceptedTasks() + " accepted, " + 
                          waitingTasks.size() + " waiting, " + 
                          statistics.getCompletedTasks() + " completed");
        System.out.println("NPU Pool: " + npuPool.getStatistics());
        System.out.println("Events remaining: " + eventQueue.size());
        System.out.println();
    }
    
    /**
     * Prints final simulation statistics.
     */
    private void printFinalStatistics() {
        System.out.println("\n=== FINAL SIMULATION STATISTICS ===");
        System.out.println("Simulation time: " + currentTime + " time units");
        System.out.println("Wall clock time: " + statistics.getSimulationDurationMs() + " ms");
        System.out.println();
        
        System.out.println("Task Statistics:");
        System.out.println("  Total tasks: " + statistics.getTotalTasks());
        System.out.println("  Accepted tasks: " + statistics.getAcceptedTasks());
        System.out.println("  Waiting tasks: " + waitingTasks.size());
        System.out.println("  Completed tasks: " + statistics.getCompletedTasks());
        System.out.println("  Acceptance rate: " + String.format("%.2f%%", statistics.getAcceptanceRate() * 100));
        System.out.println("  Average response time: " + statistics.getAverageResponseTime());
        System.out.println();
        
        System.out.println("NPU Pool Statistics:");
        NpuPool.PoolStatistics poolStats = npuPool.getStatistics();
        System.out.println("  Average compute utilization: " + String.format("%.2f%%", poolStats.getAverageComputeUtilization() * 100));
        System.out.println("  Average HBM utilization: " + String.format("%.2f%%", poolStats.getAverageHbmUtilization() * 100));
        System.out.println("  Idle NPUs: " + poolStats.getIdleNpuCount() + "/" + npuPool.getNpus().size());
        System.out.println("  Running tasks: " + poolStats.getTotalRunningTasks());
        System.out.println();
        
        System.out.println("Load Balancing Strategy: " + 
                          (npuPool.getAllocationStrategy() != null ? npuPool.getAllocationStrategy().getStrategyName() : "null"));
        System.out.println("Events processed: " + statistics.getProcessedEvents());
    }
    
    /**
     * Gets a snapshot of current simulation state.
     */
    public SimulationSnapshot getSnapshot() {
        return new SimulationSnapshot(
            currentTime,
            new ArrayList<>(waitingTasks),
            new ArrayList<>(completedTasks),
            npuPool.getStatistics(),
            statistics.copy()
        );
    }
    
    /**
     * Simulation snapshot data class.
     */
    public static class SimulationSnapshot {
        @Getter private final SimTime currentTime;
        @Getter private final List<NpuTask> waitingTasks;
        @Getter private final List<NpuTask> completedTasks;
        @Getter private final NpuPool.PoolStatistics poolStatistics;
        @Getter private final SimulationStatistics simulationStatistics;
        
        public SimulationSnapshot(SimTime currentTime, List<NpuTask> waitingTasks, 
                                List<NpuTask> completedTasks, NpuPool.PoolStatistics poolStatistics,
                                SimulationStatistics simulationStatistics) {
            this.currentTime = currentTime;
            this.waitingTasks = waitingTasks;
            this.completedTasks = completedTasks;
            this.poolStatistics = poolStatistics;
            this.simulationStatistics = simulationStatistics;
        }
    }
}
