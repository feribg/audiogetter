package com.github.feribg.audiogetter.events;

/**
 * Event representing the completion of a task as a success, failure or cancellation
 */
public class EndEvent {
    Integer taskId;
    Boolean success;
    Boolean cancelled;

    public EndEvent(Integer taskId, Boolean success, Boolean cancelled) {
        this.taskId = taskId;
        this.success = success;
        this.cancelled = cancelled;
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

    public Boolean getCancelled() {
        return cancelled;
    }

    public void setCancelled(Boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public String toString() {
        return "EndEvent{" +
                "taskId=" + taskId +
                ", success=" + success +
                ", cancelled=" + cancelled +
                '}';
    }
}
