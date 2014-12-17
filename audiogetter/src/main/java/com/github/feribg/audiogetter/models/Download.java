package com.github.feribg.audiogetter.models;

import java.io.File;
import java.io.Serializable;

/**
 * Class that described a basic download object
 */
public class Download implements Serializable {

    /**
     * Supported file types for mp3 extraction
     */
    public static final String TYPE_MP4 = "mp4";
    public static final String TYPE_MP3 = "mp3";

    /**
     * File ext of the download file
     */
    private String ext;
    /**
     * Title of the original source
     */
    private String title;
    /**
     * Filename of the download file. Must be filesys friendly
     */
    private String filename;
    /**
     * Destination for the download
     */
    private File dst;
    /**
     * Used to store the chunks for mp4 audio extraction and other TMP data
     */
    private File tmpDst;
    /**
     * Duration of the media in seconds
     */
    private Long duration;
    /**
     * File type, as defined in the cost
     */
    private String type;
    /**
     * Download service
     */
    private Services service;
    /**
     * Complete link to source URL. Usually video page on youtube or details page of a download
     */
    private String url;
    /**
     * The raw url to the media file
     */
    private String downloadUrl;
    /**
     * If the service supports IDs, for example youtube video ID
     */
    private String sourceId;
    /**
     * The extactor matched for that file type, when using the API to get music data
     */
    private String extractor;
    /**
     * Not supported yet, allow the display of thumbs for music entries
     */
    private String thumbnailUrl;
    /**
     * File size in bytes of the original media
     */
    private Long orignalSize;
    /**
     * If this is a valid mp4 container include the moov Atom fize
     */
    private Long moovAtomSize; //if source is vide get the moov atom size
    /**
     * Download size. For mp3's it is the same as originalSize, for mp4 it is the demuxed audio only data that was downloaded. (sum of all chunk sizes + request overhead)
     */
    private Long downloadSize;
    /**
     * The download taskId reference that is used to execute this download
     */
    private Integer taskId;
    /**
     * Used to show an icon for the task where applicable
     */
    protected Integer iconRes;


    public Download() {
    }

    public Integer getIconRes() {
        return iconRes;
    }

    public void setIconRes(Integer iconRes) {
        this.iconRes = iconRes;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public File getDst() {
        return dst;
    }

    public void setDst(File dst) {
        this.dst = dst;
    }

    public File getTmpDst() {
        return tmpDst;
    }

    public void setTmpDst(File tmpDst) {
        this.tmpDst = tmpDst;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }


    public Services getService() {
        return service;
    }

    public void setService(Services service) {
        this.service = service;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getExtractor() {
        return extractor;
    }

    public void setExtractor(String extractor) {
        this.extractor = extractor;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Long getOrignalSize() {
        return orignalSize;
    }

    public void setOrignalSize(Long orignalSize) {
        this.orignalSize = orignalSize;
    }

    public Long getMoovAtomSize() {
        return moovAtomSize;
    }

    public void setMoovAtomSize(Long moovAtomSize) {
        this.moovAtomSize = moovAtomSize;
    }

    public Long getDownloadSize() {
        return downloadSize;
    }

    public void setDownloadSize(Long downloadSize) {
        this.downloadSize = downloadSize;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return "Download{" +
                "ext='" + ext + '\'' +
                ", title='" + title + '\'' +
                ", filename='" + filename + '\'' +
                ", dst=" + dst +
                ", tmpDst=" + tmpDst +
                ", duration=" + duration +
                ", type='" + type + '\'' +
                ", service=" + service +
                ", url='" + url + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", sourceId='" + sourceId + '\'' +
                ", extractor='" + extractor + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", orignalSize=" + orignalSize +
                ", moovAtomSize=" + moovAtomSize +
                ", downloadSize=" + downloadSize +
                ", taskId=" + taskId +
                ", iconRes=" + iconRes +
                '}';
    }
}
