/*
 * Copyright (C) 2015 Jacek Marchwicki <jacek.marchwicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.neuifo.appkit.websocket.core;



import com.neuifo.appkit.notification.util.ObjectSerializer;
import com.neuifo.appkit.websocket.core.event.RxEventConn;
import com.neuifo.appkit.websocket.core.object.ObjectWebSocketSender;
import com.neuifo.appkit.websocket.core.object.RxObjectWebSockets;
import com.neuifo.appkit.websocket.core.object.event.RxObjectEventConn;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import okhttp3.RequestBody;
import okhttp3.ws.WebSocket;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func1;

public class RxMoreObservables {

    public static final Logger logger = Logger.getLogger("RxWebSockets");

    public RxMoreObservables() {
    }

    @Nonnull
    private static Observable<Object> sendMessage(final @Nonnull WebSocket sender, final @Nonnull String message) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                try {
                    logger.log(Level.FINE, "sendStringMessage: {0}", message);
                    sender.sendMessage(RequestBody.create(WebSocket.TEXT, message));
                    subscriber.onNext(new Object());
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * Transformer that convert String message to observable that returns if message was sent
     *
     * @param connection connection event that is used to send message
     * @return Observable that returns {@link Observer#onNext(Object)} with new Object()
     *         and {@link Observer#onCompleted()} or {@link Observer#onError(Throwable)}
     *
     * @see #sendMessage(ObjectWebSocketSender, Object)
     */
    @SuppressWarnings("unused")
    @Nonnull
    public static Observable.Transformer<String, Object> sendMessage(@Nonnull final RxEventConn connection) {
        return new Observable.Transformer<String, Object>() {
            @Override
            public Observable<Object> call(Observable<String> stringObservable) {
                return stringObservable.flatMap(new Func1<String, Observable<?>>() {
                    @Override
                    public Observable<?> call(String message) {
                        return sendMessage(connection.sender(), message);
                    }
                });
            }
        };
    }

    @Nonnull
    private static Observable<Object> sendMessage(final @Nonnull ObjectWebSocketSender sender, final @Nonnull Object message) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                try {
                    logger.log(Level.FINE, "sendStringMessage: {0}", message.toString());
                    sender.sendObjectMessage(message);
                    subscriber.onNext(message);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    logger.log(Level.FINE, "sendStringMessage: {0}", e.getLocalizedMessage());
                    //subscriber.onError(e);
                }
            }
        });
    }


    /**
     * Transformer that convert Object message to observable that returns if message was sent
     *
     * Object is parsed via {@link ObjectSerializer} given by
     * {@link RxObjectWebSockets RxObjectWebSockets(RxWebSockets, ObjectSerializer)}
     *
     * @param connection connection event that is used to send message
     * @return Observable that returns {@link Observer#onNext(Object)} with new Object()
     *         and {@link Observer#onCompleted()} or {@link Observer#onError(Throwable)}
     *
     * @see #sendMessage(RxEventConn)
     */
    @SuppressWarnings("unused")
    @Nonnull
    public static Observable.Transformer<Object, Object> sendMessage(@Nonnull final RxObjectEventConn connection) {
        return new Observable.Transformer<Object, Object>() {
            @Override
            public Observable<Object> call(Observable<Object> stringObservable) {
                return stringObservable.flatMap(new Func1<Object, Observable<?>>() {
                    @Override
                    public Observable<?> call(Object message) {
                        return sendMessage(connection.sender(), message);
                    }
                });
            }
        };
    }
}
