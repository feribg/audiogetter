package com.github.feribg.audiogetter.controllers;

import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.webkit.URLUtil;

import com.github.feribg.audiogetter.config.App;
import com.github.feribg.audiogetter.exceptions.InvalidSourceException;
import com.github.feribg.audiogetter.helpers.Utils;
import com.github.feribg.audiogetter.models.Download;
import com.github.feribg.audiogetter.models.SearchItem;
import com.github.feribg.audiogetter.models.Services;
import com.google.gson.JsonObject;

import java.io.File;

import javax.inject.Singleton;

@Singleton
public class SourceController {

    public static final String EXTRACTOR_RAW = "raw";
    public static final String EXTRACTOR_SOUNDCLOUD = "soundcloud";
    public static final String EXTRACTOR_YOUTUBE = "youtube";
    public static final String EXTRACTOR_VIMEO = "vimeo";

    /**
     * Determine the service provider given a URL.
     *
     * @param serviceName the url of the service video file
     * @return a Services enum
     * @throws Exception
     */
    public Services determineService(String serviceName) throws Exception {
        if (serviceName == null || serviceName.isEmpty()) {
            throw new InvalidSourceException("The download should contain a url");
        }
        serviceName = serviceName.toLowerCase();
        if (serviceName.equals(SourceController.EXTRACTOR_YOUTUBE)) {
            return Services.YOUTUBE;
        } else if (serviceName.equals(SourceController.EXTRACTOR_SOUNDCLOUD)) {
            return Services.SOUNDCLOUD;
        } else if (serviceName.equals(SourceController.EXTRACTOR_VIMEO)) {
            return Services.VIMEO;
        } else if (serviceName.equals(SourceController.EXTRACTOR_RAW)) {
            return Services.RAW;
        } else {
            throw new InvalidSourceException(String.format("This service is not supported. Service name: %s", serviceName));
        }

    }


    public Download extractData(JsonObject jsonObject) throws Exception {
        Download download = new Download();
        download.setUrl(jsonObject.get("url").getAsString());
        //get the first video from the array, no support for playlists yet
        JsonObject videoObject = jsonObject.getAsJsonArray("videos").get(0).getAsJsonObject();
        Log.d(App.TAG, "Video data: " + videoObject.toString());
        String downloadUrl = videoObject.get("url").getAsString();
        String downloadTitle = videoObject.get("title").getAsString();
        String sourceId = videoObject.get("id").getAsString();
        String ext = videoObject.get("ext").getAsString();
        String extractor = videoObject.get("extractor").getAsString();
        if (downloadUrl == null || !URLUtil.isValidUrl(downloadUrl) || sourceId == null || ext == null || extractor == null) {
            throw new InvalidSourceException();
        }
        String filename = Utils.cleanFilename(downloadTitle);
        //if cleanup strips the entire name set the source ID as filename
        if (filename.length() == 0) {
            filename = sourceId;
        }
        download.setTitle(downloadTitle);
        download.setFilename(filename);
        download.setDownloadUrl(downloadUrl);
        download.setType(ext); //only support youtube MP4s
        download.setExt(ext); //only support youtube MP4s
        download.setService(determineService(extractor));
        download.setSourceId(sourceId);
        download.setDuration(videoObject.get("duration").getAsLong());
        download.setFolder(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/samples"));
        download.setDst(new File(download.getFolder(), download.getTitle() + "." + download.getExt()));
        download.setTmpDst(new File(download.getFolder(), download.getTitle() + "-temp." + download.getExt()));
        download.setTmpDst2(new File(download.getFolder(), download.getTitle() + "-temp-aac." + download.getExt()));
        download.setExtractor(extractor);
        Log.d(App.TAG, "Download object: " + download.toString());
        return download;
    }


    public Download extractFromSearchItem(SearchItem searchItem) throws Exception {
        Download download = new Download();
        download.setType(searchItem.getFormat()); //only support youtube MP3s
        download.setExt(searchItem.getFormat()); //only support youtube MP3s
        download.setSourceId(searchItem.getSongId());
        download.setTitle(String.format("%s - %s", searchItem.getArtistName(), searchItem.getTitle()));
        download.setDownloadUrl(searchItem.getSongLocation());
        download.setUrl(searchItem.getUrl());
        download.setFolder(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/samples"));
        download.setDst(new File(download.getFolder(), download.getTitle() + "." + download.getExt()));
        download.setExtractor(searchItem.getExtractor());
        download.setService(determineService(searchItem.getExtractor()));
        String filename = Utils.cleanFilename(download.getTitle());
        //if cleanup strips the entire name set the source ID as filename
        if (filename.length() == 0) {
            filename = download.getSourceId();
        }
        download.setFilename(filename);
        return download;
    }

    public SearchItem extractXiamiSearchItemData(JsonObject songObject) {
        if (songObject == null) {
            return null;
        }
        SearchItem searchItem = new SearchItem();
        if (songObject.get("album_name") != null) {
            searchItem.setAlbumName(Html.fromHtml(songObject.get("album_name").getAsString()).toString());
        }
        searchItem.setFormat("mp3"); //only support MP3s for xiami
        searchItem.setSongLogo(songObject.get("album_logo").getAsString());
        searchItem.setArtistName(Html.fromHtml(songObject.get("artist_name").getAsString()).toString());
        searchItem.setTitle(Html.fromHtml(songObject.get("name").getAsString()).toString());
        searchItem.setSongLocation(songObject.get("location").getAsString());
        searchItem.setAlbumId(songObject.get("album_id").getAsString());
        searchItem.setArtistId(songObject.get("artist_id").getAsString());
        searchItem.setSongId(songObject.get("song_id").getAsString());
        searchItem.setExtractor(SourceController.EXTRACTOR_RAW);
        return searchItem;
    }

    public SearchItem extractSoundcloudSearchItem(JsonObject songObject) {
        if (songObject == null) {
            return null;
        }
        //if type is not track or the song hasnt finished encoding dont add as a result
        if (!songObject.get("kind").getAsString().toLowerCase().equals("track") ||
                !songObject.get("state").getAsString().toLowerCase().equals("finished") ||
                songObject.get("permalink_url").isJsonNull()) {
            return null;
        }
        SearchItem searchItem = new SearchItem();
        searchItem.setTitle(songObject.get("title").getAsString().trim());
        if (songObject.get("artwork_url") != null && !songObject.get("artwork_url").isJsonNull()) {
            searchItem.setSongLogo(songObject.get("artwork_url").getAsString());
        }
        searchItem.setFormat(songObject.get("original_format").getAsString());
        if (!songObject.get("genre").isJsonNull()) {
            searchItem.setGenre(songObject.get("genre").getAsString());
        }
        if (songObject.get("label_name") != null && !songObject.get("label_name").isJsonNull()) {
            searchItem.setAlbumName(songObject.get("label_name").getAsString());
        }
        searchItem.setExtractor(SourceController.EXTRACTOR_SOUNDCLOUD); //only support youtube MP4s
        searchItem.setUrl(songObject.get("permalink_url").getAsString());
        return searchItem;
    }

    public SearchItem extractVimeoSearchItem(JsonObject songObject) {
        if (songObject == null) {
            return null;
        }
        //if type is not track or the song hasnt finished encoding dont add as a result
        if (!songObject.get("status").getAsString().toLowerCase().equals("available") ||
                songObject.get("link").isJsonNull()) {
            return null;
        }
        SearchItem searchItem = new SearchItem();
        searchItem.setFormat("mp4");
        searchItem.setTitle(songObject.get("name").getAsString().trim());
        searchItem.setExtractor(SourceController.EXTRACTOR_VIMEO); //only support youtube MP4s
        searchItem.setUrl(songObject.get("link").getAsString());
        return searchItem;
    }
}
