package com.neuifo.appkit.websocket.exp;

import java.io.IOException;

public class SocketExceptionCountWrapper extends IOException {
    private int failingCount = 0;
    private boolean isUnsubcribed = false;


    public SocketExceptionCountWrapper(String message, Throwable cause, int failingCount, boolean isUnsubcribed) {
        super(message, cause);
        this.failingCount = failingCount;
        this.isUnsubcribed = isUnsubcribed;
    }

    public SocketExceptionCountWrapper(Throwable cause, int failingCount, boolean isUnsubcribed) {
        super(cause);
        this.failingCount = failingCount;
        this.isUnsubcribed = isUnsubcribed;
    }
}
