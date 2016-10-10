package com.example;

/**
 * Project "EEEEeeee"
 * <p/>
 * Created by Lukasz Marczak
 * on 10.10.16.
 */
public class NoPrimaryKeyException extends RuntimeException {
    public NoPrimaryKeyException() {
        super("At least one filed should be annotated with @" + SQLitePrimaryKey.class.getSimpleName());
    }
}
