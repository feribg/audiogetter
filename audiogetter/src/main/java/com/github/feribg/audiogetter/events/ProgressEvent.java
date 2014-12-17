package com.github.feribg.audiogetter.events;

import com.github.feribg.audiogetter.models.Download;

/**
 * Event representing the change in progress for a given task
 */
public class ProgressEvent {
    Download download;
    Long completed;
    Long total;

    public ProgressEvent(Download download, Long completed, Long total) {
        this.download = download;
        this.completed = completed;
        this.total = total;
    }

    public Download getDownload() {
        return download;
    }

    public void setDownload(Download download) {
        this.download = download;
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
                "download=" + download +
                ", completed=" + completed +
                ", total=" + total +
                '}';
    }
}
