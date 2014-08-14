package com.github.feribg.audiogetter.ui.activities;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import com.bugsense.trace.BugSenseHandler;
import com.github.feribg.audiogetter.R;
import com.github.feribg.audiogetter.config.App;
import com.github.feribg.audiogetter.services.ManagerService;

import roboguice.activity.RoboActivity;

public class MainActivity extends RoboActivity {

    ManagerService managerService;
    boolean serviceBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            managerService = ((ManagerService.ManagerBinder) binder).getService();
            serviceBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            managerService = null;
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            BugSenseHandler.initAndStartSession(this, App.BUGSENSE_API);

            setContentView(R.layout.activity_main);
            bindService(new Intent(this, ManagerService.class), mConnection, Context.BIND_AUTO_CREATE);


        } catch (Exception e) {
            Log.d(App.TAG, "Exception:" + e);
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this, ManagerService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                Intent intent = new Intent(this, AddActivity.class);
                this.startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


}
