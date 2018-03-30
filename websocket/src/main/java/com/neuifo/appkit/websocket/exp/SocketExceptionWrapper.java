package com.neuifo.appkit.websocket.exp;

import java.io.IOException;

public class SocketExceptionWrapper extends IOException {

    private boolean isUnsubscribed;


    public SocketExceptionWrapper(boolean isUnsubscribed, String message, Throwable cause) {
        super(message, cause);
        this.isUnsubscribed = isUnsubscribed;
    }

    @Override
    public Throwable getCause() {
        return super.getCause();
    }

    public boolean isUnsubscribed() {
        return isUnsubscribed;
    }

    public void setUnsubscribed(boolean unsubscribed) {
        isUnsubscribed = unsubscribed;
    }

    @Override
    public String toString() {
        return "SocketExceptionWrapper{" +
                "isUnsubscribed=" + isUnsubscribed +
                "} " + super.toString();
    }
}
