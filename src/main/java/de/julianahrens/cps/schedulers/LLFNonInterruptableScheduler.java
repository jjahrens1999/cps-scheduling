package de.julianahrens.cps.schedulers;

import de.julianahrens.cps.schedulers.util.Task;

import java.util.List;

public class LLFNonInterruptableScheduler extends Scheduler {

    @Override
    public List<Integer> getNextTaskId(int currentTime, int lastTaskId) {
        int nextTaskId = tasks.stream()
                .filter(task -> task.deadline() - task.rest() > currentTime)
                .min((t1, t2) -> {
                    double laxity1 = getLaxity(t1, currentTime);
                    double laxity2 = getLaxity(t2, currentTime);
                    return Double.compare(laxity1, laxity2);
                })
                .map(Task::id)
                .orElse(-1);

        if (nextTaskId != -1) {
            removeFromScheduler(nextTaskId);
        }

        return List.of(nextTaskId, 1);
    }
}
