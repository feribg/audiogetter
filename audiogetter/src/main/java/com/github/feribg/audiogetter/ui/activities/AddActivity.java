package com.github.feribg.audiogetter.ui.activities;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.github.feribg.audiogetter.R;
import com.github.feribg.audiogetter.config.App;
import com.github.feribg.audiogetter.config.Constants;
import com.github.feribg.audiogetter.controllers.SourceController;
import com.github.feribg.audiogetter.exceptions.InvalidSourceException;
import com.github.feribg.audiogetter.helpers.Utils;
import com.github.feribg.audiogetter.models.Download;
import com.github.feribg.audiogetter.services.ManagerService;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class AddActivity extends RoboActivity {

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
    @InjectView(R.id.button_add)
    Button addButton;
    @Inject
    SourceController sourceController;
    @InjectView(R.id.sourceInput)
    EditText sourceInput;
    @InjectView(R.id.downloadProgress)
    ProgressBar progressBar;
    private volatile Boolean inProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, App.BUGSENSE_API);

        setContentView(R.layout.activity_add);
        bindService(new Intent(this, ManagerService.class), mConnection,
                Context.BIND_AUTO_CREATE);

        if (serviceBound)
            Log.d(App.TAG, "ManagerService is bound in the AddActivity");


        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                List<String> detectedUrls = Utils.extractUrls(sharedText);
                if (detectedUrls != null && detectedUrls.size() > 0) {
                    sharedText = detectedUrls.get(0); //get the first url
                }
                sourceInput.setText(sharedText);
            }
        }

        //handle the process action
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (inProgress) {
                        Toast.makeText(App.ctx, "Please wait for the current download to begin before starting a new one", Toast.LENGTH_LONG).show();
                        return;
                    }
                    inProgress = true;
                    progressBar.setVisibility(View.VISIBLE);
                    String source = sourceInput.getText().toString();
                    if (!URLUtil.isValidUrl(source)) {
                        throw new InvalidSourceException();
                    }
                    List<NameValuePair> qparams = new ArrayList<NameValuePair>();
                    qparams.add(new BasicNameValuePair("url", source));
                    URI uri = Utils.getUri(Constants.Backend.API_SCHEME, "/api/info", qparams);
                    Ion.with(App.ctx)
                            .load(uri.toString())
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                    try {
                                        Download dl = sourceController.extractData(result);
                                        if (dl.getDst() != null && dl.getDst().exists()) {
                                            Toast.makeText(App.ctx, "This file already exists in your library", Toast.LENGTH_LONG).show();
                                        } else {
                                            showConfirmation(dl);
                                        }
                                    } catch (Exception ex) {
                                        Log.d(App.TAG, "Error while trying to fetch video data", ex);
                                        Toast.makeText(App.ctx, "We cannot process this URL", Toast.LENGTH_LONG).show();
                                    } finally {
                                        inProgress = false;
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                } catch (InvalidSourceException e) {
                    Toast.makeText(App.ctx, "We cannot process this URL", Toast.LENGTH_LONG).show();
                    Log.d(App.TAG, "Unsupported source", e);
                } catch (Exception e) {
                    Toast.makeText(App.ctx, "There was an error trying to download this file", Toast.LENGTH_LONG).show();
                    Log.e(App.TAG, "Error while trying to fetch video data", e);
                }
            }
        });
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

    /**
     * Show the confirmation for downloading a file
     *
     * @param download
     */
    private void showConfirmation(final Download download) {
        String msgText;
        if (download.getDuration() != null && download.getDuration() > App.ADVISED_DURATION) {
            msgText = String.format(getResources().getString(R.string.confirmation_text_long), download.getTitle(), download.getDuration() / 60);
        } else {
            msgText = String.format(getResources().getString(R.string.confirmation_text), download.getTitle(), download.getDuration() / 60);
        }
        //Ask the user if they want to quit
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.long_download_title)
                .setMessage(msgText)
                .setPositiveButton(R.string.confirm_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        managerService.startTask(download);
                    }
                })
                .setNegativeButton(R.string.confirm_cancel, null)
                .show();
    }
}