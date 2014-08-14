package com.github.feribg.audiogetter.tasks;


import android.util.Log;

import com.github.feribg.audiogetter.config.App;
import com.github.feribg.audiogetter.events.ProgressEvent;
import com.github.feribg.audiogetter.events.StartEvent;
import com.github.feribg.audiogetter.helpers.Utils;
import com.github.feribg.audiogetter.models.Download;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.EventBus;
import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

public class RawAudioTask extends BaseTask {

    public RawAudioTask(Integer id, Download download, int imageRes) {
        super(id, download, imageRes);
        final RoboInjector injector = RoboGuice.getInjector(App.ctx);
        injector.injectMembersWithoutViews(this);

    }

    @Override
    public void run() {
        try {
            EventBus.getDefault().post(new StartEvent(taskID));
            //only run if thread hasn't been interrupted
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            Long startTime = System.currentTimeMillis();

            //set reference to the thread that's running the task so it can be interrupted
            setCurrentThread(Thread.currentThread());
            mBuilder.setContentText("Download in progress");
            notificationManager.notify(taskID, mBuilder.build());
            final AtomicInteger completedDownloads = new AtomicInteger(0);
            Log.d(App.TAG, "URL to execute: " + download.getDownloadUrl());
            Future<File> file = Ion.with(App.ctx)
                    .load(download.getDownloadUrl())
                    .progress(new ProgressCallback() {
                        @Override
                        public void onProgress(long downloaded, long total) {
                            EventBus.getDefault().post(new ProgressEvent(taskID, downloaded, total));
                            mBuilder.setProgress(100, Utils.normalizePercent(downloaded, total), false);
                            notificationManager.notify(taskID, mBuilder.build());
                        }
                    })
                    .group(dlGroup)
                    .write(download.getDst())
                    .setCallback(new FutureCallback<File>() {
                        @Override
                        public void onCompleted(Exception e, File file) {
                            if (e == null && file != null) {
                                completedDownloads.incrementAndGet();
                            }
                        }
                    });

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            file.get();

            if (completedDownloads.get() != 1) {
                throw new Exception("Download seems to have failed");
            }

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            //change the notification to the complete state and index the file
            complete();

            Long endTime = System.currentTimeMillis();
            Log.d(App.TAG, "Total execution time: " + (endTime - startTime) + "ms");

        } catch (InterruptedException ex) {
            Log.d(App.TAG, ex.toString());
            cancelled();
            cleanup(true);
        } catch (Exception ex) {
            Log.d(App.TAG, ex.toString());
            failed();
            cleanup(true);
        } finally {
            Ion.getDefault(App.ctx).cancelAll(dlGroup);
            cleanup(false);
        }
    }

    private void cleanup(Boolean removeOutputFile) {
        if (removeOutputFile && download.getDst() != null) {
            FileUtils.deleteQuietly(download.getDst());
        }
        if (download.getTmpDst() != null) {
            FileUtils.deleteQuietly(download.getTmpDst());
        }
        if (download.getTmpFolder() != null) {
            FileUtils.deleteQuietly(download.getTmpFolder());
        }
        if (download.getTmpDst2() != null) {
            FileUtils.deleteQuietly(download.getTmpDst2());
        }
    }

}

