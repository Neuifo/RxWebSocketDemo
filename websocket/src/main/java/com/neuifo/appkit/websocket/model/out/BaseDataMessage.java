package com.neuifo.appkit.websocket.model.out;


/**
 * Created by neuifo on 2017/9/28.
 */

public interface BaseDataMessage<T> {

    void setData(T object);

    T getData();
}
