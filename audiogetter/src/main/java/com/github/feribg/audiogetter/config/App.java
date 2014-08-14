package com.github.feribg.audiogetter.config;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.koushikdutta.ion.Ion;

import roboguice.RoboGuice;


public class App extends Application {

    public static final String TAG = "audiogetter";
    public static final String BUGSENSE_API = "eb8d5560";
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String PREFS_NAME = "com.github.feribg.audiogetter";
    public static final Long ADVISED_DURATION = 900L;
    public static SharedPreferences settings;

    public static Context ctx;
    private static App instance;

    public App() {
        instance = this;
    }

    /**
     * @return the current application instance
     */
    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        BugSenseHandler.initAndStartSession(getApplicationContext(), App.BUGSENSE_API);

        settings = getSharedPreferences(PREFS_NAME, 0);
        App.ctx = getApplicationContext();

        super.onCreate();
        RoboGuice.setBaseApplicationInjector(this, RoboGuice.DEFAULT_STAGE,
                RoboGuice.newDefaultRoboModule(this), new AppModule());

        Ion.getDefault(App.ctx).configure().setLogging(TAG, Log.DEBUG);
    }


}
