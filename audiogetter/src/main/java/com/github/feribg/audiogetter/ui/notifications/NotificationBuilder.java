package com.github.feribg.audiogetter.ui.notifications;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.github.feribg.audiogetter.R;
import com.github.feribg.audiogetter.config.App;
import com.github.feribg.audiogetter.helpers.Utils;
import com.github.feribg.audiogetter.models.Download;
import com.github.feribg.audiogetter.services.ManagerService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.File;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

/**
 * This class is used to manage the notifications for downloads lifecycle and other events
 */
@Singleton
public class NotificationBuilder {

    @Inject
    android.app.NotificationManager notificationManager;
    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(App.ctx);

    public NotificationBuilder(){
        //init Robo injector without views
        final RoboInjector injector = RoboGuice.getInjector(App.ctx);
        injector.injectMembersWithoutViews(this);
    }

    /**
     * Build a notification for a successfully completed download
     * @param download
     */
    public void downloadNotificationCompleteSuccess(Download download){
        // When the loop is finished, updates the notification
        Intent playIntent = new Intent();
        playIntent.setAction(android.content.Intent.ACTION_VIEW);
        File file = download.getDst();
        playIntent.setDataAndType(Uri.fromFile(file), "audio/*");
        PendingIntent pPlayIntent = PendingIntent.getActivity(App.ctx, 0, playIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder = new NotificationCompat.Builder(App.ctx);
        mBuilder.setContentTitle(String.format("Downloading \"%s\"", download.getTitle()))
                .setContentText("Download complete")
                .setOngoing(false);
        if(download.getIconRes() != null){
            mBuilder.setSmallIcon(download.getIconRes());
        }
        mBuilder.setProgress(0, 0, false)
                .setContentIntent(pPlayIntent);
        notificationManager.notify(download.getTaskId(), mBuilder.build());
    }

    /**
     * Notification when download is downloaded to the queue and pending processing, with a cancel button
     * @param download
     */
    public void downloadNotificationPending(Download download) {
        Intent intent = new Intent(ManagerService.INTENT_CANCEL);
        intent.putExtra("id", download.getTaskId());
        intent.setAction(ManagerService.INTENT_CANCEL);
        PendingIntent pIntent = PendingIntent.getBroadcast(App.ctx, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        mBuilder.setContentTitle(String.format("Downloading \"%s\"", download.getTitle()))
                .setContentText("Download pending").addAction(R.drawable.ic_action_cancel, "Cancel", pIntent);
        if(download.getIconRes() != null){
            mBuilder.setSmallIcon(download.getIconRes());
        }
        mBuilder.setOngoing(true);
        notificationManager.notify(download.getTaskId(), mBuilder.build());
    }

    /**
     * Show this notificaiton if the user manually cancelled a download
     * @param download
     */
    public void downloadNotificationCancelled(Download download){
        mBuilder = new NotificationCompat.Builder(App.ctx);
        mBuilder.setContentTitle(String.format("Downloading \"%s\"", download.getTitle()))
                .setContentText("Download was cancelled")
                .setOngoing(false);
        if(download.getIconRes() != null){
            mBuilder.setSmallIcon(download.getIconRes());
        }
        mBuilder.setProgress(0, 0, false);
        notificationManager.notify(download.getTaskId(), mBuilder.build());
    }

    /**
     * Download has started notification
     * @param download
     */
    public void downloadNotificationStarted(Download download){
        mBuilder.setContentText("Download in progress");
        notificationManager.notify(download.getTaskId(), mBuilder.build());
    }

    /**
     * Show this notification if the download failed unexpectedly
     * @param download
     */
    public void downloadNotificationFailed(Download download){
        mBuilder = new NotificationCompat.Builder(App.ctx);
        mBuilder.setContentTitle(String.format("Downloading \"%s\"", download.getTitle()))
                .setContentText("Download failed. Please try again.")
                .setOngoing(false);
        if(download.getIconRes() != null){
            mBuilder.setSmallIcon(download.getIconRes());
        }
        mBuilder.setProgress(0, 0, false);
        notificationManager.notify(download.getTaskId(), mBuilder.build());
    }

    /**
     * Show a finalizing audio message, when extracting from a video
     * @param download
     */
    public void downloadNotificationFinalize(Download download){
        mBuilder.setContentText("Finalizing the audio");
        mBuilder.setProgress(0, 0, true);
        notificationManager.notify(download.getTaskId(), mBuilder.build());
    }

    /**
     * Redraw notification for progress changes
     * @param download
     * @param progress
     * @param total
     */
    public void downloadNotificationProgress(Download download, Long progress, Long total){
        mBuilder.setProgress(100, Utils.normalizePercent(progress, total), false);
        notificationManager.notify(download.getTaskId(), mBuilder.build());
    }
}
