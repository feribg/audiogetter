package com.github.feribg.audiogetter.exceptions;

public class InvalidSourceException extends Exception {

    public String message = "Invalid video source url";
    public int code = 101;

    public InvalidSourceException() {
    }

    public InvalidSourceException(String message) {
        super(message);
    }

}
