package com.github.feribg.audiogetter.events;

/**
 * Event representing the change in progress for a given task
 */
public class ProgressEvent {
    Integer taskId;
    Long completed;
    Long total;

    public ProgressEvent(Integer taskId, Long completed, Long total) {
        this.taskId = taskId;
        this.completed = completed;
        this.total = total;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Long getCompleted() {
        return completed;
    }

    public void setCompleted(Long completed) {
        this.completed = completed;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "ProgressEvent{" +
                "taskId=" + taskId +
                ", completed=" + completed +
                ", total=" + total +
                '}';
    }
}
