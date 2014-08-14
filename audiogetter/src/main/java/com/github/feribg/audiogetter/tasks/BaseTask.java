package com.github.feribg.audiogetter.tasks;


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

public abstract class BaseTask implements Runnable {
    protected final Integer taskID;
    protected Download download;
    protected Object dlGroup = new Object();
    protected Integer iconRes;
    //progress counter for the download job
    protected AtomicInteger progress = new AtomicInteger(0);
    @Inject
    NotificationManager notificationManager;
    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(App.ctx);
    private Thread mCurrentThread;

    public BaseTask(Integer taskID, Download download, int iconRes) {
        final RoboInjector injector = RoboGuice.getInjector(App.ctx);
        injector.injectMembersWithoutViews(this);
        this.taskID = taskID;
        this.download = download;
        this.iconRes = iconRes;
        //set the temp folder to hold the chunks
        download.setTmpFolder(new File(download.getFolder().getAbsolutePath() + File.separator + taskID));
        if (!download.getTmpFolder().exists()) {
            download.getTmpFolder().mkdir();
        }
        buildNotification();
    }

    private void buildNotification() {
        Intent intent = new Intent(ManagerService.INTENT_CANCEL);
        intent.putExtra("id", taskID);
        intent.setAction(ManagerService.INTENT_CANCEL);
        PendingIntent pIntent = PendingIntent.getBroadcast(App.ctx, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        mBuilder.setContentTitle(String.format("Downloading \"%s\"", download.getTitle()))
                .setContentText("Download pending").addAction(R.drawable.ic_action_cancel, "Cancel", pIntent)
                .setSmallIcon(iconRes)
                .setOngoing(true);

        notificationManager.notify(taskID, mBuilder.build());
    }

    /*
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

    /*
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
        EventBus.getDefault().post(new EndEvent(taskID, true, false));
        MediaScannerConnection.scanFile(App.ctx, new String[]{download.getDst().getAbsolutePath()}, null, null);
        // When the loop is finished, updates the notification
        Intent playIntent = new Intent();
        playIntent.setAction(android.content.Intent.ACTION_VIEW);
        File file = download.getDst();
        playIntent.setDataAndType(Uri.fromFile(file), "audio/*");
        PendingIntent pPlayIntent = PendingIntent.getActivity(App.ctx, 0, playIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder = new NotificationCompat.Builder(App.ctx);
        mBuilder.setContentTitle(String.format("Downloading \"%s\"", download.getTitle()))
                .setContentText("Download complete")
                .setOngoing(false)
                .setSmallIcon(iconRes)
                .setProgress(0, 0, false)
                .setContentIntent(pPlayIntent);
        notificationManager.notify(taskID, mBuilder.build());
    }

    protected void cancelled() {
        EventBus.getDefault().post(new EndEvent(taskID, false, true));
        mBuilder = new NotificationCompat.Builder(App.ctx);
        mBuilder.setContentTitle(String.format("Downloading \"%s\"", download.getTitle()))
                .setContentText("Download was cancelled")
                .setOngoing(false)
                .setSmallIcon(iconRes)
                .setProgress(0, 0, false);
        notificationManager.notify(taskID, mBuilder.build());
    }

    protected void failed() {
        EventBus.getDefault().post(new EndEvent(taskID, false, false));
        mBuilder = new NotificationCompat.Builder(App.ctx);
        mBuilder.setContentTitle(String.format("Downloading \"%s\"", download.getTitle()))
                .setContentText("Download failed. Please try again.")
                .setOngoing(false)
                .setSmallIcon(iconRes)
                .setProgress(0, 0, false);
        notificationManager.notify(taskID, mBuilder.build());
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
