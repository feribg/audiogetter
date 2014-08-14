package com.github.feribg.audiogetter.ui.activities;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.SearchView;

import com.github.feribg.audiogetter.R;
import com.github.feribg.audiogetter.config.App;
import com.github.feribg.audiogetter.services.ManagerService;
import com.github.feribg.audiogetter.ui.adapters.SearchPagerAdapter;
import com.github.feribg.audiogetter.ui.fragments.SearchResultsBaseFragment;

import roboguice.activity.RoboFragmentActivity;

public class SearchActivity extends RoboFragmentActivity {

    SearchPagerAdapter searchPagerAdapter;
    ViewPager viewPager;

    ManagerService managerService;
    boolean serviceBound = false;
    /**
     * Bind the downloader service to the current activity
     */
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
    String searchTerm;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, ManagerService.class), mConnection,
                Context.BIND_AUTO_CREATE);
        if (serviceBound)
            Log.d(App.TAG, "ManagerService is bound in the SearchNewActivity");
        setContentView(R.layout.activity_search);

        searchPagerAdapter = new SearchPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(searchPagerAdapter);

        viewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
//                        performSearch(searchTerm);
                    }
                }
        );

        handleIntent(getIntent());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);
        searchView.clearFocus();

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchView.setQuery(query, false);
        }

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this, ManagerService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            performSearch(query);
        }
    }


    private void performSearch(String query) {
        searchTerm = query.trim();
        searchPagerAdapter.setSearchTerm(searchTerm);
        SearchResultsBaseFragment fragment = (SearchResultsBaseFragment) searchPagerAdapter.getCurrentFragment();
        if (fragment != null) {
            fragment.performSearch(query);
        }
    }

    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        Log.d(App.TAG, "SAVING:" + searchTerm);
        savedState.putString("searchTerm", searchTerm);

    }
}
