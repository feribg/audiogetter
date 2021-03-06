package com.github.feribg.audiogetter.events;

import com.github.feribg.audiogetter.models.Download;

/**
 * Event representing the beginning of a running task
 */
public class StartEvent {
    Download download;

    public StartEvent(Download download) {
        this.download = download;
    }

    public Download getDownload() {
        return download;
    }

    public void setDownload(Download download) {
        this.download = download;
    }

    @Override
    public String toString() {
        return "StartEvent{" +
                "download=" + download +
                '}';
    }
}
