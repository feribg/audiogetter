package com.github.feribg.audiogetter.events;

import com.github.feribg.audiogetter.models.Download;

/**
 * Event representing the addition of a task to the executor queue and if it succeeded or failed
 */
public class AddEvent {
    /**
     * The download object for that task
     */
    Download download;
    /**
     * If it was added succesfully or there was an error submitting to the executor
     */
    Boolean success;

    public AddEvent(Download dl, Boolean success) {
        this.success = success;
        this.download = dl;
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
                ", download=" + download +
                ", success=" + success +
                '}';
    }

    public Download getDownload() {
        return download;
    }

    public void setDownload(Download download) {
        this.download = download;
    }

}
