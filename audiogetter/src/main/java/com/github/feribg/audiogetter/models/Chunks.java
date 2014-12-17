package com.github.feribg.audiogetter.models;

import com.coremedia.iso.boxes.SampleSizeBox;

import java.util.ArrayList;

/**
 * Representation of the audio chunks in an mp4 container
 */
public class Chunks {
    /**
     * List of chunks
     */
    ArrayList<Chunk> chunks;

    /**
     * The mp4 container sample size box, used to determine the size of each chunk
     */
    SampleSizeBox sampleSizeBox;

    public Chunks(ArrayList<Chunk> chunks, SampleSizeBox sampleSizeBox) {
        this.chunks = chunks;
        this.sampleSizeBox = sampleSizeBox;
    }

    public ArrayList<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(ArrayList<Chunk> chunks) {
        this.chunks = chunks;
    }

    public SampleSizeBox getSampleSizeBox() {
        return sampleSizeBox;
    }

    public void setSampleSizeBox(SampleSizeBox sampleSizeBox) {
        this.sampleSizeBox = sampleSizeBox;
    }
}
