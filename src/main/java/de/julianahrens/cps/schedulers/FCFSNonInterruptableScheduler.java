package de.julianahrens.cps.schedulers;

import de.julianahrens.cps.schedulers.util.Task;

import java.util.Comparator;
import java.util.List;

public class FCFSNonInterruptableScheduler extends Scheduler {

    @Override
    public List<Integer> getNextTaskId(int currentTime, int lastTaskId) {
        int nextTaskId = tasks.stream()
                .filter(task -> task.deadline() - task.rest() > currentTime)
                .min(Comparator.comparingInt(Task::startTime))
                .map(Task::id)
                .orElse(-1);

        if (nextTaskId != -1) {
            removeFromScheduler(nextTaskId);
        }

        return List.of(nextTaskId, 1);
    }
}
