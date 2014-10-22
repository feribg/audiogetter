package com.github.feribg.audiogetter.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.feribg.audiogetter.R;
import com.github.feribg.audiogetter.config.App;
import com.github.feribg.audiogetter.helpers.Utils;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URI;

/**
 * Search and display mp3Skull search results
 */
public class Mp3skullSearchResultsFragment extends SearchResultsBaseFragment{
    //this fragments loads all results at once so we just track if loaded or not
    Boolean loaded = false;
    Future<Response<String>> searchResultsFuture;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.search_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        savedState.putBoolean("loaded", loaded);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void cleanup() {
        super.cleanup();
        loaded = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadMore() {
        try {
            // don't attempt to load more if a load is already in progress
            if (searchResultsFuture != null && !searchResultsFuture.isDone() && !searchResultsFuture.isCancelled())
                return;
            // we have loaded the max number of results, dont load more
            if (loaded) {
                if (searchFooterView != null) {
                    getListView().removeFooterView(searchFooterView);
                }
                return;
            }
            String baseUrl = Utils.getMp3SkullBaseURI().toString();
            Ion.with(this)
                    .load(baseUrl)
                    .asString()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> result) {
                            try{
                                if(e != null){
                                    throw e;
                                }
                                //extract the CSRF token before making the search request
                                Document doc = Jsoup.parse(result.getResult());
                                Element csrfElement = doc.select("input[name=fckh]").first();
                                if(csrfElement == null){
                                    throw new Exception("Cannot find CSRF element on the page");
                                }
                                URI uri = Utils.getMp3SkullSearchURI(searchTerm, csrfElement.attr("value"));
                                searchResultsFuture = Ion.with(App.ctx)
                                        .load(uri.toString())
                                        .asString()
                                        .withResponse()
                                        .setCallback(new FutureCallback<Response<String>>() {
                                            @Override
                                            public void onCompleted(Exception e, Response<String> response) {
                                                try {
                                                    //TODO: implement parsing of the serch results and populate the list
                                                    loaded = true;
                                                } catch (Exception ex) {
                                                    Toast.makeText(getActivity(), R.string.error_loading_results, Toast.LENGTH_LONG).show();
                                                    Log.e(App.TAG, "Error while trying to get search results", ex);
                                                }

                                            }
                                        });
                            }catch (Exception ex){
                                Toast.makeText(getActivity(), R.string.error_loading_results, Toast.LENGTH_LONG).show();
                                Log.e(App.TAG, "Error while trying to get search results", ex);
                                Log.e(App.TAG, "Response: "+ result.toString());
                            }

                        }
                    });
        } catch (Exception e) {
            Log.e(App.TAG, "There was an error loading search results", e);
            Toast.makeText(getActivity(), R.string.error_loading_results, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Called on activity change state to load the previous results and page states
     *
     * @param savedInstanceState the current activity saved state
     */
    @Override
    protected void loadState(Bundle savedInstanceState) {
        super.loadState(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("loaded")) {
                loaded = savedInstanceState.getBoolean("loaded");
            }
        }
    }
}
