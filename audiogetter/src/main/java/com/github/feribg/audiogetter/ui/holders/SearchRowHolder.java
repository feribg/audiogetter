package com.github.feribg.audiogetter.ui.holders;


import android.view.View;
import android.widget.TextView;

import com.github.feribg.audiogetter.R;

public class SearchRowHolder {

    TextView songName;
    TextView albumTitle;

    public SearchRowHolder(View v) {
        songName = (TextView) v.findViewById(R.id.songName);
        albumTitle = (TextView) v.findViewById(R.id.albumTitle);
    }

    public TextView getSongName() {
        return songName;
    }

    public void setSongName(TextView songName) {
        this.songName = songName;
    }

    public TextView getAlbumTitle() {
        return albumTitle;
    }

    public void setAlbumTitle(TextView albumTitle) {
        this.albumTitle = albumTitle;
    }
}
