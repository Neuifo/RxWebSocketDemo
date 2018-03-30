package com.neuifo.appkit.websocket;

/**
 * 基本用列的回调，默认空实现
 */
public class SocketErrorHandler<T> extends rx.Subscriber<T> {

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onNext(T t) {

    }
}
