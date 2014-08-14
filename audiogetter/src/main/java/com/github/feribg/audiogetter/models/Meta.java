package com.github.feribg.audiogetter.models;

/**
 * Class that described meta information about the video.
 */
public class Meta {
    public String num;
    public String type;
    public String ext;

    public Meta(String num, String ext, String type) {
        this.num = num;
        this.ext = ext;
        this.type = type;
    }
}
