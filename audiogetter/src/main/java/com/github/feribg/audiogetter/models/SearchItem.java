package com.github.feribg.audiogetter.models;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchItem implements Parcelable {

    public static final Parcelable.Creator<SearchItem> CREATOR = new Parcelable.Creator<SearchItem>() {
        public SearchItem createFromParcel(Parcel in) {
            return new SearchItem(in);
        }

        public SearchItem[] newArray(int size) {
            return new SearchItem[size];
        }
    };
    private String songId;
    private String title;
    private String songLocation;
    private String songLogo;
    private String albumName;
    private String albumId;
    private String artistName;
    private String artistId;
    private String format;
    private String genre;
    private String extractor;
    private String meta;
    private String url;

    public SearchItem() {

    }

    public SearchItem(Parcel dest) {
        this.songId = dest.readString();
        this.title = dest.readString();
        this.songLocation = dest.readString();
        this.songLogo = dest.readString();
        this.albumName = dest.readString();
        this.albumId = dest.readString();
        this.artistName = dest.readString();
        this.artistId = dest.readString();
        this.extractor = dest.readString();
        this.format = dest.readString();
        this.genre = dest.readString();
        this.url = dest.readString();
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getExtractor() {
        return extractor;
    }

    public void setExtractor(String extractor) {
        this.extractor = extractor;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSongLocation() {
        return songLocation;
    }

    public void setSongLocation(String songLocation) {
        this.songLocation = songLocation;
    }

    public String getSongLogo() {
        return songLogo;
    }

    public void setSongLogo(String songLogo) {
        this.songLogo = songLogo;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(songId);
        dest.writeString(title);
        dest.writeString(songLocation);
        dest.writeString(songLogo);
        dest.writeString(albumName);
        dest.writeString(albumId);
        dest.writeString(artistName);
        dest.writeString(artistId);
        dest.writeString(extractor);
        dest.writeString(format);
        dest.writeString(genre);
        dest.writeString(url);
    }

    @Override
    public String toString() {
        return "SearchItem{" +
                "songId='" + songId + '\'' +
                ", title='" + title + '\'' +
                ", songLocation='" + songLocation + '\'' +
                ", songLogo='" + songLogo + '\'' +
                ", albumName='" + albumName + '\'' +
                ", albumId='" + albumId + '\'' +
                ", artistName='" + artistName + '\'' +
                ", artistId='" + artistId + '\'' +
                ", format='" + format + '\'' +
                ", genre='" + genre + '\'' +
                ", extractor='" + extractor + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
