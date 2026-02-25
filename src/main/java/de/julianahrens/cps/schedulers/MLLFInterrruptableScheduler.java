package de.julianahrens.cps.schedulers;

import de.julianahrens.cps.schedulers.util.Task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MLLFInterrruptableScheduler extends Scheduler {
    private record TaskLaxity(int taskId, int laxity) {
    }

    private record TaskLaxityInversionDuration(int taskId, int laxityInversionDuration) {
    }

    private final List<TaskLaxityInversionDuration> taskLaxityInversionDurations = new ArrayList<>();

    @Override
    public List<Integer> getNextTaskId(int currentTime, int lastTaskId) {
        // Check if a task still has laxity inversion duration left and return it if so
        Optional<TaskLaxityInversionDuration> taskWithLaxityInversionDurationLeft = taskLaxityInversionDurations.stream()
                .filter(task -> task.taskId == lastTaskId)
                .findAny();

        // Check if the task has not finished yet and is still in the scheduler
        Optional<Task> lastTask = tasks.stream()
                .filter(task -> task.id() == lastTaskId)
                .findAny();

        // If the task has laxity inversion duration left and has not finished yet, return it
        // Check for lastTask is necessary as task execution might be blocked by waiting for parallel tasks run on other stations
        if (taskWithLaxityInversionDurationLeft.isPresent() && lastTask.isPresent()) {
            if (taskWithLaxityInversionDurationLeft.get().laxityInversionDuration < lastTask.get().rest()) {
                removeFromScheduler(lastTaskId);
                return List.of(lastTaskId, 0);
            }
        }
        // Otherwise, run the MLLF scheduling algorithm to determine the next task to execute

        // Filter out expired tasks
        List<Task> nonExpiredTasks = tasks.stream()
                .filter(task -> task.deadline() - task.rest() > currentTime)
                .toList();

        // Map the laxities of all non expired tasks to a list of TaskLaxity objects
        List<TaskLaxity> laxities = nonExpiredTasks.stream()
                .map(task -> new TaskLaxity(task.id(), getLaxity(task, currentTime)))
                .toList();

        // Determine the task with the least laxity
        Optional<TaskLaxity> taskWithLeastLaxity = laxities.stream()
                .min(Comparator.comparingInt(TaskLaxity::laxity));

        // Return -1 if there are no tasks that can meet their deadline
        if (taskWithLeastLaxity.isEmpty()) {
            return List.of(-1, 0);
        }

        // Find all tasks having the same least laxity as the task with the least laxity as determined in the last step
        List<TaskLaxity> tasksWithLeastLaxity = laxities.stream()
                .filter(task -> task.laxity() == taskWithLeastLaxity.get().laxity())
                .toList();

        // Return the task id if there is one task with the unique least laxity
        if (tasksWithLeastLaxity.size() == 1) {
            removeFromScheduler(taskWithLeastLaxity.get().taskId);
            return List.of(taskWithLeastLaxity.get().taskId, 0);
        }

        // Determine task with the lowest deadline among the tasks with the least laxity
        Optional<Task> taskWithLeastLaxityAndLowestDeadline = nonExpiredTasks.stream()
                .filter(task -> tasksWithLeastLaxity.stream().map(TaskLaxity::taskId).toList().contains(task.id()))
                .min(Comparator.comparingInt(Task::deadline));

        Optional<TaskLaxity> taskLaxityWithLeastLaxityAndLowestDeadline = tasksWithLeastLaxity.stream()
                .filter(taskLaxity -> taskLaxity.taskId() == taskWithLeastLaxityAndLowestDeadline.get().id())
                .findAny();

        // Determine task with the lowest deadline that is not among the tasks not having the least laxity
        Optional<Task> taskWithLowestDeadline = nonExpiredTasks.stream()
                .filter(task -> !tasksWithLeastLaxity.stream().map(TaskLaxity::taskId).toList().contains(task.id()))
                .min(Comparator.comparingInt(Task::deadline));

        // If there is no task with a lower deadline than the task with the least laxity and lowest deadline, return the task with the least laxity and lowest deadline
        if (taskWithLowestDeadline.isEmpty()) {
            TaskLaxityInversionDuration newTask = new TaskLaxityInversionDuration(taskWithLeastLaxityAndLowestDeadline.get().id(), 0);
            taskLaxityInversionDurations.add(newTask);
            removeFromScheduler(taskWithLeastLaxityAndLowestDeadline.get().id());
            return List.of(taskWithLeastLaxityAndLowestDeadline.get().id(), 0);
        }

        // Determine Laxity Inversion Duration
        int laxityInversionDuration = (taskWithLowestDeadline.get().deadline() - currentTime) - taskLaxityWithLeastLaxityAndLowestDeadline.get().laxity;
        TaskLaxityInversionDuration newTask = new TaskLaxityInversionDuration(taskWithLeastLaxityAndLowestDeadline.get().id(), taskWithLeastLaxityAndLowestDeadline.get().rest() - laxityInversionDuration);
        taskLaxityInversionDurations.add(newTask);

        // Return the task
        removeFromScheduler(taskWithLeastLaxityAndLowestDeadline.get().id());
        return List.of(taskWithLeastLaxityAndLowestDeadline.get().id(), 0);
    }

    @Override
    public void addToScheduler(int id, int startTime, int deadline, int rest, int numRequired) {
        super.addToScheduler(id, startTime, deadline, rest, numRequired);

        // Trigger rescheduling based on appearance of a new task, but only if it is actually a new task
        if (!taskLaxityInversionDurations.stream().map(TaskLaxityInversionDuration::taskId).toList().contains(id)) {
            taskLaxityInversionDurations.clear();
        }
    }
}
