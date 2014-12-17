package com.github.feribg.audiogetter.models;

/**
 * Single audio chunk in an mp4 container, containing the number(order in which it appears),
 * start bytes, end bytes and the offset from the beginning of the file
 */
public class Chunk {

    /**
     * Order number of this chunk in the sequence
     */
    public int number;
    /**
     * Start byte number
     */
    public long start;
    /**
     * End byte number
     */
    public long end;
    /**
     * Offset from the beginning of the file
     */
    private Long offset; //offset in the file that has to be written to

    public Chunk(int number, long start, long end, Long offset) {
        this.number = number;
        this.start = start;
        this.end = end;
        this.offset = offset;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
