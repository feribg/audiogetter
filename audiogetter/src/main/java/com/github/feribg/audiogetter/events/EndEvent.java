package com.github.feribg.audiogetter.events;

import com.github.feribg.audiogetter.models.Download;

/**
 * Event representing the completion of a task as a success, failure or cancellation
 */
public class EndEvent {
    Download download;
    Boolean success;
    Boolean cancelled;

    public EndEvent(Download download, Boolean success, Boolean cancelled) {
        this.download = download;
        this.success = success;
        this.cancelled = cancelled;
    }

    public Download getDownload() {
        return download;
    }

    public void setDownload(Download download) {
        this.download = download;
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
                "download=" + download +
                ", success=" + success +
                ", cancelled=" + cancelled +
                '}';
    }
}
