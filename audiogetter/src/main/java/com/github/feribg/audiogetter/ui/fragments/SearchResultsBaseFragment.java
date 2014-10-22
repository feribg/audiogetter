package com.github.feribg.audiogetter.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.feribg.audiogetter.R;
import com.github.feribg.audiogetter.config.App;
import com.github.feribg.audiogetter.controllers.SourceController;
import com.github.feribg.audiogetter.models.Download;
import com.github.feribg.audiogetter.models.SearchItem;
import com.github.feribg.audiogetter.services.ManagerService;
import com.github.feribg.audiogetter.ui.holders.SearchRowHolder;

import java.util.ArrayList;

import javax.inject.Inject;

import roboguice.fragment.RoboListFragment;


public abstract class SearchResultsBaseFragment extends RoboListFragment {

    @Inject
    protected SourceController sourceController;

    /**
     * Search listview footer, containing the loader progressbar
     */
    protected View searchFooterView;

    /**
     * Last search term used in this activity,
     * to track whether the term changed and a new search is necessary
     */
    protected String searchTerm;

    /**
     * Array adapter managing the results list
     */
    protected ArrayAdapter<SearchItem> searchResultsAdapter;

    /**
     * The actual ArrayList holding the results
     */
    protected ArrayList<SearchItem> results = new ArrayList<SearchItem>();


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Inflate the layout for this fragment
        searchFooterView = getActivity().getLayoutInflater().inflate(R.layout.search_list_footer, null);
        getListView().addFooterView(searchFooterView, null, false);
        loadState(savedInstanceState);
        Bundle args = getArguments();
        String searchQuery = args.getString("search_term");
        searchResultsAdapter = new ArrayAdapter<SearchItem>(getActivity(), 0, results) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                SearchRowHolder searchRowHolder;
                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.search_list_row, null);
                    searchRowHolder = new SearchRowHolder(convertView);
                    convertView.setTag(searchRowHolder);
                } else {
                    searchRowHolder = (SearchRowHolder) convertView.getTag();
                }

                SearchItem searchItem = getItem(position);
                searchRowHolder.getSongName().setText(searchItem.getTitle());
                searchRowHolder.getAlbumTitle().setText(searchItem.getGenre());

                // we're near the end of the list adapter, so load more items
                if (position >= getCount() - 3)
                    loadMore();

                return convertView;
            }
        };
        performSearch(searchQuery);
        getListView().setAdapter(searchResultsAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        SearchItem searchItem = (SearchItem) l.getItemAtPosition(position);
        if (searchItem != null) {
            handleClick(searchItem);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        Log.d(App.TAG, "SAVING:" + searchTerm);
        savedState.putParcelableArrayList("results", results);
        savedState.putString("searchTerm", searchTerm);
    }

    /**
     * Load saved fragment state
     *
     * @param savedInstanceState
     */
    protected void loadState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("results")) {
                results = savedInstanceState.getParcelableArrayList("results");
            }
            if (savedInstanceState.containsKey("searchTerm")) {
                searchTerm = savedInstanceState.getString("searchTerm");
                Log.d(App.TAG, "loaded search term" + searchTerm);

            }
        }
    }

    /**
     * Handle the clicked search item
     *
     * @param searchItem the search item object that was clicked in the list view
     */
    protected void handleClick(SearchItem searchItem) {
        try {
            showConfirmation(sourceController.extractFromSearchItem(searchItem));
        } catch (Exception e) {
            Log.e(App.TAG, "Unable to get song details", e);
            Toast.makeText(getActivity(), R.string.error_loading_details, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Perform a new search, clearing the old activity state and populating with the new results
     *
     * @param query the search query text
     */
    public void performSearch(String query) {
        Log.d(App.TAG, "Search: " + query);
        if (searchTerm != null && !searchTerm.equals(query.trim()) && query.trim().length() > 0) {
            Log.d(App.TAG, String.format("Old query was: \"%s\", new query is \"%s\"", searchTerm, query.trim()));
            searchTerm = query.trim();
            cleanup();
            loadMore();
        } else if (searchTerm == null && query.trim().length() > 0) {
            Log.d(App.TAG, String.format("Old query was: null, new query is \"%s\"", query.trim()));
            searchTerm = query.trim();
            cleanup();
            loadMore();
        } else {
            Log.d(App.TAG, "Skipping search, terms are idential");
        }
    }

    /**
     * Used to async load more results given the current list items and pagination status
     */
    protected void loadMore() {

    }

    /**
     * Clear the current activity state, resetting all pagination and results.
     * Used before performing a new search
     */
    protected void cleanup() {
        searchResultsAdapter.clear();
        //if the view is gone, recreate
        if (getListView().getFooterViewsCount() == 0) {
            searchFooterView = getLayoutInflater(null).inflate(R.layout.search_list_footer, null);
            getListView().addFooterView(searchFooterView, null, false);
        }
    }

    /**
     * Show the confirmation for downloading a file
     *
     * @param download the download object to send to the downloader service
     */
    protected void showConfirmation(final Download download) {
        String msgText = String.format(getResources().getString(R.string.search_download_confirm_message), download.getTitle());

        //Ask the user if they want to quit
        new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.long_download_title)
                .setMessage(msgText)
                .setPositiveButton(R.string.confirm_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(ManagerService.INTENT_DOWNLOAD);
                        i.putExtra("download", download);
                        getActivity().sendBroadcast(i);
                        Log.d(App.TAG, "download broadcast was sent");
                    }
                })
                .setNegativeButton(R.string.confirm_cancel, null)
                .show();
    }
}
