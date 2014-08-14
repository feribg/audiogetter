package com.github.feribg.audiogetter.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.github.feribg.audiogetter.R;
import com.github.feribg.audiogetter.config.App;
import com.github.feribg.audiogetter.events.AddEvent;
import com.github.feribg.audiogetter.events.EndEvent;
import com.github.feribg.audiogetter.events.ProgressEvent;
import com.github.feribg.audiogetter.events.StartEvent;
import com.github.feribg.audiogetter.models.Download;
import com.github.feribg.audiogetter.tasks.BaseTask;
import com.github.feribg.audiogetter.tasks.RawAudioTask;
import com.github.feribg.audiogetter.tasks.SoundcloudTask;
import com.github.feribg.audiogetter.tasks.VimeoTask;
import com.github.feribg.audiogetter.tasks.YoutubeTask;
import com.koushikdutta.ion.Ion;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.EventBus;
import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

/**
 * Service implementation to monitor worker progress and manage async tasks in background
 */
public class ManagerService extends Service {

    //Lock used for the delete
    public static final Object mSync = new Object();
    public static final String INTENT_CANCEL = "com.github.feribg.audiogetter.intent_cancel";
    public static final String INTENT_DOWNLOAD = "com.github.feribg.audiogetter.intent_download";
    // Sets the initial threadpool size
    private static final int CORE_POOL_SIZE = 1;
    // Sets the maximum threadpool size to 10
    private static final int MAXIMUM_POOL_SIZE = 10;
    //threadpool keep alive timeout in secs
    private static final int KEEP_ALIVE_TIME = 1;
    //generate IDs to assign to new tasks
    public static AtomicInteger idGenerator = new AtomicInteger();
    private static TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    //binder used to bind to activities
    private final IBinder mBinder = new ManagerBinder();
    // A queue of Runnables for the image download pool
    private final BlockingQueue<Runnable> mTaskQueue;
    // A managed pool of background download threads
    private final ThreadPoolExecutor mWorkerThreadPool;
    //holds a list of current tasks submitted to the thread pool
    private Map<Integer, BaseTask> tasksMap = new ConcurrentHashMap<Integer, BaseTask>();


    public ManagerService() {
        mTaskQueue = new LinkedBlockingQueue<Runnable>();
        mWorkerThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mTaskQueue);
        final RoboInjector injector = RoboGuice.getInjector(App.ctx);
        // This will inject all fields marked with the @Inject annotation
        injector.injectMembersWithoutViews(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ManagerService.INTENT_CANCEL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Given a download object, initilize the correct download task and pass it to the thread pool
     *
     * @param download download object containing the source data
     */
    public void startTask(Download download) {
        BaseTask task = null;
        Integer taskId = idGenerator.incrementAndGet();
        try {
            switch (download.getService()) {
                case YOUTUBE:
                    task = new YoutubeTask(taskId, download, R.drawable.notification_youtube);
                    break;
                case SOUNDCLOUD:
                    task = new SoundcloudTask(taskId, download, R.drawable.notification_soundcloud);
                    break;
                case VIMEO:
                    task = new VimeoTask(taskId, download, R.drawable.notification_vimeo);
                    break;
                case RAW:
                    task = new RawAudioTask(taskId, download, R.drawable.notification_music);
                    break;
            }
            if (task != null) {
                mWorkerThreadPool.execute(task);
                tasksMap.put(taskId, task);
                EventBus.getDefault().post(new AddEvent(taskId, true));
                Log.d(App.TAG, "Added youtube task to the queue");
            } else {
                Toast.makeText(App.ctx, "Unsupported source URL", Toast.LENGTH_LONG).show();
            }
        } catch (RejectedExecutionException ex) {
            Toast.makeText(App.ctx, R.string.toast_too_many_concurrent, Toast.LENGTH_LONG).show();
            Log.e(App.TAG, "Execution of task #" + taskId + " was rejected by the executor", ex);
        }

    }

    /**
     * Stops a download Thread and removes it from the thread pool
     */
    public void removeTask(BaseTask baseTask) {

        // If the Thread object still exists and the download matches the specified URL
        if (baseTask != null) {

            /*
             * Locks on this class to ensure that other processes aren't mutating Threads.
             */
            synchronized (mSync) {

                // Gets the Thread that the downloader task is running on
                Thread thread = baseTask.getCurrentThread();
                //cancel all pending downloads in the event loop
                Ion.getDefault(App.ctx).cancelAll(baseTask.getDlGroup());
                // If the Thread exists, posts an interrupt to it
                if (null != thread)
                    thread.interrupt();
            }
            /*
             * Removes the download Runnable from the ThreadPool. This opens a Thread in the
             * ThreadPool's work queue, allowing a task in the queue to start.
             */
            this.mTaskQueue.remove(baseTask);
            this.tasksMap.remove(baseTask.getTaskID());
            Log.d(App.TAG, "Removed task #" + baseTask.getTaskID());
        }
    }

    /**
     * Handle a task progress event
     *
     * @param progressEvent
     */
    public void onEvent(ProgressEvent progressEvent) {
        Log.d(App.TAG, progressEvent.toString());
    }

    /**
     * Invoked when a task execution has began and its running in its thread
     *
     * @param startEvent
     */
    public void onEvent(StartEvent startEvent) {
        Log.d(App.TAG, startEvent.toString());
    }

    /**
     * Invoked when a task is queued in the thread pool, but waiting for execution
     *
     * @param addEvent
     */
    public void onEvent(AddEvent addEvent) {
        Log.d(App.TAG, addEvent.toString());
    }

    /**
     * Invoked when a task finishes with either success or failure status or its cancelled
     *
     * @param endEvent
     */
    public void onEvent(EndEvent endEvent) {
        Log.d(App.TAG, endEvent.toString());
    }

    public Map<Integer, BaseTask> getTasksMap() {
        return tasksMap;
    }

    /**
     * Used to bind this service to activities
     */
    public class ManagerBinder extends Binder {
        public ManagerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ManagerService.this;
        }
    }

}
