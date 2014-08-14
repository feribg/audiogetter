package com.github.feribg.audiogetter.models;

public class Chunk {
    public int number;
    public long start;
    public long end;
    private Long offset; //offset in the file that has to be written to

    public Chunk(int number, long start, long end) {
        this.number = number;
        this.start = start;
        this.end = end;
    }

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
