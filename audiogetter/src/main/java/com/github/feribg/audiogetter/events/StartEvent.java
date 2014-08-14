package com.github.feribg.audiogetter.events;

/**
 * Event representing the beginning of a running task
 */
public class StartEvent {
    Integer taskId;

    public StartEvent(Integer taskId) {
        this.taskId = taskId;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return "ProgressEvent{" +
                "taskId=" + taskId +
                '}';
    }
}
