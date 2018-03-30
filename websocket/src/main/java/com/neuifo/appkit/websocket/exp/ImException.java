package com.neuifo.appkit.websocket.exp;

/**
 * Created by neuifo on 16/9/9.
 * 用于处理Http的错误
 */
public class ImException extends Exception {//Unauthorized

    public static final String ERROR_MSG_SOCKET_UNAVAILABLE = "IM链接失败";
    public static final int ERROR_IO_NET_UNAVAILABLE = 2002;
    public static final int ERROR_IO_EXCEPTION = 2001;

    private int errCode;
    private String displayMessage;

    public ImException(Throwable throwable, int errCode) {
        super(throwable);
        this.errCode = errCode;
    }

    public ImException(Throwable throwable, int errCode, String msg) {
        super(msg);
        this.errCode = errCode;
        this.displayMessage = msg;
    }

    public ImException(int errCode, String msg) {
        super(msg);
        this.errCode = errCode;
        this.displayMessage = msg;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public int getErrCode() {
        return this.errCode;
    }

    public void setDisplayMessage(String msg) {
        this.displayMessage = msg;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    @Override
    public String toString() {
        return displayMessage;
    }

    public static ImException createNetworkError() {
        ImException ex = new ImException(ImException.ERROR_IO_NET_UNAVAILABLE, ImException.ERROR_MSG_SOCKET_UNAVAILABLE);
        return ex;
    }

}
