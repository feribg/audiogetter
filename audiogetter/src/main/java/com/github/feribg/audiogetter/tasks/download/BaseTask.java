package com.github.feribg.audiogetter.tasks.download;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.github.feribg.audiogetter.R;
import com.github.feribg.audiogetter.config.App;
import com.github.feribg.audiogetter.events.EndEvent;
import com.github.feribg.audiogetter.models.Download;
import com.github.feribg.audiogetter.services.ManagerService;
import com.google.inject.Inject;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.EventBus;
import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

/**
 * Base download task
 */
public abstract class BaseTask implements Runnable {
    /**
     * TaskId to track the task in the Task Manager
     */
    protected final Integer taskID;
    /**
     * The Download object on which this task is working
     */
    protected Download download;

    /**
     * Used to group all requests for this task, in order to cancel all if the task is interrupted
     */
    protected Object dlGroup = new Object();

    /**
     * Track the progress of this task
     */
    protected AtomicInteger progress = new AtomicInteger(0);

    /**
     * Reference to the current thread running this task
     */
    private Thread mCurrentThread;

    public BaseTask(Integer taskID, Download download) {
        //inject the proper services
        final RoboInjector injector = RoboGuice.getInjector(App.ctx);
        injector.injectMembersWithoutViews(this);

        this.taskID = taskID;
        this.download = download;
    }



    /**
     * Returns the Thread that this Task is running on. The method must first get a lock on a
     * static field, in this case the ThreadPool singleton. The lock is needed because the
     * Thread object reference is stored in the Thread object itself, and that object can be
     * changed by processes outside of this app.
     */
    public Thread getCurrentThread() {
        synchronized (ManagerService.mSync) {
            return mCurrentThread;
        }
    }

    /**
     * Sets the identifier for the current Thread. This must be a synchronized operation; see the
     * notes for getCurrentThread()
     */
    public void setCurrentThread(Thread thread) {
        synchronized (ManagerService.mSync) {
            mCurrentThread = thread;
        }
    }

    public Object getDlGroup() {
        return dlGroup;
    }

    protected void complete() {
        MediaScannerConnection.scanFile(App.ctx, new String[]{download.getDst().getAbsolutePath()}, null, null);
        EventBus.getDefault().post(new EndEvent(download, true, false));
    }

    protected void cancelled() {
        EventBus.getDefault().post(new EndEvent(download, false, true));
    }

    protected void failed() {
        EventBus.getDefault().post(new EndEvent(download, false, false));

    }

    public Download getDownload() {
        return download;
    }

    public void setDownload(Download download) {
        this.download = download;
    }


    public Integer getTaskID() {
        return taskID;
    }
}
