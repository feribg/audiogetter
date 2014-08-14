package com.github.feribg.audiogetter.models;

import com.coremedia.iso.boxes.SampleSizeBox;

import java.util.ArrayList;


public class Chunks {
    ArrayList<Chunk> chunks;

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
