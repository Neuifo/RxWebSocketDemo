package com.neuifo.appkit.model.socket;

import com.neuifo.appkit.websocket.model.out.BaseDataMessage;
import com.neuifo.appkit.websocket.model.out.PingMessage;
import com.neuifo.appkit.websocket.model.out.RegisteredMessage;

/**
 * Created by neuifo on 2017/10/25.
 */

public class SimpleBaseSocketMessage<T> implements PingMessage, RegisteredMessage, BaseDataMessage<T> {

    private Short ver = 0x0100;

    private Object body;

    private String msgId;

    private String time;


    public Short getVer() {
        return ver;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    @Override
    public void setData(T object) {
        setBody(object);
    }

    @Override
    public T getData() {
        return (T) getBody();
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
