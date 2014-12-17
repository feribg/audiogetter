package com.github.feribg.audiogetter.events.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.github.feribg.audiogetter.config.App;
import com.github.feribg.audiogetter.models.Download;
import com.github.feribg.audiogetter.services.ManagerService;
import com.github.feribg.audiogetter.tasks.download.BaseTask;


public class ManagerServiceReceiver extends BroadcastReceiver {


    public ManagerServiceReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        IBinder binder = peekService(context, new Intent(context, ManagerService.class));
        if (binder == null)
            return;
        ManagerService managerService = ((ManagerService.ManagerBinder) binder).getService();
        String action = intent.getAction();
        if (action.equals(ManagerService.INTENT_CANCEL)) {
            Integer iTaskID = (Integer) intent.getExtras().get("id");
            Log.d(App.TAG, "Received a task delete broadcast for task #" + iTaskID);
            if (iTaskID != null) {
                BaseTask task = managerService.getTasksMap().get(iTaskID);
                managerService.removeTask(task);
            }
        } else if (action.equals(ManagerService.INTENT_DOWNLOAD)) {
            Download download = (Download) intent.getExtras().get("download");
            Log.d(App.TAG, "Received a new download task broadcast for download url" + download.getDownloadUrl());
            if (download.getDownloadUrl() != null) {
                managerService.startTask(download);
            }
        }
    }
}
