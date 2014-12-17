package com.github.feribg.audiogetter.events;

import com.github.feribg.audiogetter.models.Download;

/**
 * Finalize event, trigger when assembling audio file from a video extraction
 */
public class FinalizeEvent {
    Download download;

    public FinalizeEvent(Download download) {
        this.download = download;
    }

    public Download getDownload() {
        return download;
    }

    public void setDownload(Download download) {
        this.download = download;
    }
}
