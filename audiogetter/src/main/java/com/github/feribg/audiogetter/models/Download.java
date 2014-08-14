package com.github.feribg.audiogetter.models;

import java.io.File;
import java.io.Serializable;

/**
 * Class that described a basic download object
 */
public class Download implements Serializable {

    public static final String TYPE_MP4 = "mp4";

    private String ext; //filename extension (to determine format)
    private String title;  //original title of the source
    private String filename; //a filesystem friendly title
    private File dst;
    private File tmpDst;
    private File tmpDst2;
    private File folder;
    private File tmpFolder;
    private Long duration;
    private String type; // file type
    private Services service;
    private String url; //complete url to the original source
    private String downloadUrl; //the url to the raw download media
    private String sourceId;
    private String extractor;
    private String thumbnailUrl;
    private Long orignalSize; //original file size of the source
    private Long moovAtomSize; //if source is vide get the moov atom size
    private Long downloadSize; //the new download size after calculations (applicable to mp4s that are demuxed on the fly)


    public Download() {
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public File getTmpFolder() {
        return tmpFolder;
    }

    public void setTmpFolder(File tmpFolder) {
        this.tmpFolder = tmpFolder;
    }

    public File getFolder() {
        return folder;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }

    public File getTmpDst2() {
        return tmpDst2;
    }

    public void setTmpDst2(File tmpDst2) {
        this.tmpDst2 = tmpDst2;
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

    @Override
    public String toString() {
        return "Download{" +
                "ext='" + ext + '\'' +
                ", title='" + title + '\'' +
                ", filename='" + filename + '\'' +
                ", dst=" + dst +
                ", tmpDst=" + tmpDst +
                ", tmpDst2=" + tmpDst2 +
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
                '}';
    }
}
