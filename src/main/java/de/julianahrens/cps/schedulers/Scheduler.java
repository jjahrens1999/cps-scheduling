package de.julianahrens.cps.schedulers;

import de.julianahrens.cps.schedulers.util.Task;

import java.util.*;

public abstract class Scheduler {

    public List<Task> tasks = new ArrayList<>();
    public Map<Integer, Integer> partialTasks = new HashMap<>() {};

    public void addToScheduler(int id, int startTime, int deadline, int rest, int numRequired) {
        if (numRequired == 1) {
            tasks.add(new Task(id, startTime, deadline, rest));
            return;
        }
        if (partialTasks.containsKey(id) && partialTasks.get(id) == numRequired - 1) {
            tasks.add(new Task(id, startTime, deadline, rest));
            return;
        }
        if (partialTasks.containsKey(id)) {
            partialTasks.put(id, partialTasks.get(id) + 1);
        } else {
            partialTasks.put(id, 1);
        }
    }

    public void removeFromScheduler(int id) {
        tasks.removeIf(task -> task.id() == id);
    }

    protected int getLaxity(Task task, int currentTime) {
        return (task.deadline() - currentTime) - task.rest();
    }

    public abstract List<Integer> getNextTaskId(int currentTime, int lastTaskId);
}
