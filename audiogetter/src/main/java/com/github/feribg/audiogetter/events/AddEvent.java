package com.github.feribg.audiogetter.events;

/**
 * Event representing the addition of a task to the executor queue and if it succeeded or failed
 */
public class AddEvent {
    Integer taskId;
    Boolean success;

    public AddEvent(Integer taskId, Boolean success) {
        this.taskId = taskId;
        this.success = success;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "AddEvent{" +
                "taskId=" + taskId +
                ", success=" + success +
                '}';
    }
}
