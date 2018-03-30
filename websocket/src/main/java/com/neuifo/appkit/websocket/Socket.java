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

package com.neuifo.appkit.websocket;


import android.support.annotation.NonNull;


import com.neuifo.appkit.websocket.core.RxMoreObservables;
import com.neuifo.appkit.websocket.core.object.event.RxObjectEvent;
import com.neuifo.appkit.websocket.core.object.event.RxObjectEventConn;
import com.neuifo.appkit.websocket.core.object.event.RxObjectEventConnected;
import com.neuifo.appkit.websocket.core.object.event.RxObjectEventDisconnected;
import com.neuifo.appkit.websocket.core.object.event.RxObjectEventMessage;
import com.neuifo.appkit.websocket.model.out.BaseDataMessage;
import com.neuifo.appkit.websocket.model.out.PingMessage;
import com.neuifo.appkit.websocket.model.out.RegisteredMessage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import okhttp3.ws.WebSocket;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class Socket {
    public static final Logger LOGGER = Logger.getLogger("Rx");

    private final Observable<RxObjectEvent> events;
    private final Observable<Object> connection;
    private final BehaviorSubject<RxObjectEventConn> connectedAndRegistered;
    @Nonnull
    private final Scheduler scheduler;
    //private RegisteredMessage registeredMessage;
    private PingMessage pingMessage;
    private final Class<? extends BaseDataMessage> target;


    public Socket(@Nonnull SocketConnection socketConnection, @Nonnull Scheduler scheduler, RegisteredMessage registeredMessage, PingMessage pingMessage, Class<? extends BaseDataMessage> target) {
        this.scheduler = scheduler;
        final PublishSubject<RxObjectEvent> events = PublishSubject.create();
        connection = socketConnection
                .connection()
                .lift(new OperatorDoOnNext<>(events))
                .lift(MoreObservables.ignoreNext())
                .compose(MoreObservables.behaviorRefCount());//鬼知道这里干了啥，导致数据传不过去
        this.events = events;

        final Observable<RxObjectEventMessage> rxObjectEventMessageObservable = events
                .compose(MoreObservables.filterAndMap(RxObjectEventMessage.class))
                .filter(new FilterRegisterMessage());

        final Observable<RxObjectEventDisconnected> disconnectedMessage = events
                .compose(MoreObservables.filterAndMap(RxObjectEventDisconnected.class));

        //this.registeredMessage = registeredMessage;
        this.pingMessage = pingMessage;

        connectedAndRegistered = BehaviorSubject.create((RxObjectEventConn) null);

        disconnectedMessage.map(new Func1<RxObjectEventDisconnected, RxObjectEventConn>() {
            @Override
            public RxObjectEventConn call(RxObjectEventDisconnected rxEventDisconnected) {
                //LogHelper.getInstance().e("disconnectedMessage", rxEventDisconnected.toString());
                return null;
            }
        }).mergeWith(rxObjectEventMessageObservable).subscribe(connectedAndRegistered);

        // Register on connected
        final Observable<RxObjectEventConnected> connectedMessage = events
                .compose(MoreObservables.filterAndMap(RxObjectEventConnected.class))
                .lift(LoggingObservables.<RxObjectEventConnected>loggingLift(LOGGER, "ConnectedEvent"));

        connectedMessage.flatMap(new FlatMapToRegisterMessage(registeredMessage))
                .lift(LoggingObservables.loggingOnlyErrorLift(LOGGER, "SendRegisterEvent"))
                .onErrorReturn(MoreObservables.throwableToIgnoreError())
                .subscribe();

        // Log events
        LOGGER.setLevel(Level.ALL);
        RxMoreObservables.logger.setLevel(Level.ALL);
        events.subscribe(LoggingObservables.logging(LOGGER, "Events"));
        connectedAndRegistered.subscribe(LoggingObservables.logging(LOGGER, "ConnectedAndRegistered"));
        this.target = target;
    }

    public Observable<RxObjectEvent> events() {
        return events;
    }

    public Observable<RxObjectEventConn> connectedAndRegistered() {
        return connectedAndRegistered;
    }

    public Observable<Object> connection() {
        return connection;
    }

    public Subscription sendPingInterval(int time) {
        return Observable.combineLatest(Observable.interval(time, TimeUnit.SECONDS, scheduler), connectedAndRegistered, new Func2<Long, RxObjectEventConn, RxObjectEventConn>() {
            @Override
            public RxObjectEventConn call(Long aLong, RxObjectEventConn rxEventConn) {
                return rxEventConn;
            }
        }).compose(isConnected()).flatMap(new Func1<RxObjectEventConn, Observable<?>>() {
            @Override
            public Observable<?> call(RxObjectEventConn rxEventConn) {
                return Observable.just(pingMessage).compose(RxMoreObservables.sendMessage(rxEventConn));
            }
        }).subscribe();
    }

    public void sendPingEvery5seconds() {
        Observable.interval(5, TimeUnit.SECONDS, scheduler).flatMap(new Func1<Long, Observable<?>>() {
            @Override
            public Observable<?> call(Long aLong) {
                return connectedAndRegistered.compose(isConnected()).first().flatMap(new Func1<RxObjectEventConn, Observable<?>>() {
                    @Override
                    public Observable<?> call(RxObjectEventConn rxEventConn) {
                        return Observable.just(pingMessage).compose(RxMoreObservables.sendMessage(rxEventConn));
                    }
                });
            }
        }).subscribe();
    }

    private final Object lock = new Object();
    private int counter = 0;

    @Nonnull
    public Observable<String> nextId() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                final int current;
                synchronized (lock) {
                    current = counter;
                    counter += 1;
                }
                subscriber.onNext(String.valueOf(current));
                subscriber.onCompleted();
            }
        });
    }

    @Nonnull
    public Observable<Object> sendMessageOnceWhenConnected(final Func1<String, Observable<Object>> createMessage) {
        return connectedAndRegistered.compose(isConnected())
                .first().flatMap(new Func1<RxObjectEventConn, Observable<Object>>() {
                    @Override
                    public Observable<Object> call(final RxObjectEventConn rxEventConn) {
                        return requestData(rxEventConn, createMessage);
                    }
                });
    }

    @NonNull
    private Observable<Object> requestData(final RxObjectEventConn rxEventConn, final Func1<String, Observable<Object>> createMessage) {

        return nextId().flatMap(new Func1<String, Observable<Object>>() {
            @Override
            public Observable<Object> call(final String messageId) {
                // 发送消息
                final Observable<Object> sendMessageObservable =
                        createMessage.call(messageId)
                                .compose(RxMoreObservables.sendMessage(rxEventConn));
                return sendMessageObservable;
            }
        });
    }

    /**
     * 主动发送消息，根据参数决定是否等待返回结果
     *
     * @param wait 是否等待返回结果
     */
    @NonNull
    public Observable<Object> sendMessageOnceWhenConnectedV2(final boolean wait, final Func1<String, Object> createMessage) {
        return connectedAndRegistered.compose(isConnected()).first().flatMap(new Func1<RxObjectEventConn, Observable<Object>>() {
            @Override
            public Observable<Object> call(final RxObjectEventConn rxEventConn) {
                return requestData(wait, rxEventConn, createMessage);
            }
        });
    }

    @NonNull
    private Observable<Object> requestData(final boolean wait, final RxObjectEventConn rxEventConn, final Func1<String, Object> createMessage) {
        return nextId().flatMap(new Func1<String, Observable<Object>>() {
            @Override
            public Observable<Object> call(final String messageId) {

                // 发送消息

                Object id = null;
                final Object msg = createMessage.call(messageId);
                /*if (msg instanceof WbsMessage) {
                    id = ((WbsMessage) msg).getMessageId();

                    if (id == null) {
                        id = messageId;
                    }
                }*/

                final Observable<Object> sendMessageObservable = Observable.defer(new Func0<Observable<Object>>() {
                    @Override
                    public Observable<Object> call() {
                        return Observable.just(msg);
                    }
                }).compose(RxMoreObservables.sendMessage(rxEventConn));

                if (!wait) {
                    return sendMessageObservable;
                }
                final Observable<? extends BaseDataMessage> waitForResponseObservable = events
                        .doOnNext(new Action1<RxObjectEvent>() {
                            @Override
                            public void call(RxObjectEvent rxObjectEvent) {
                                //LogHelper.getInstance().e("waitEvent:" + rxObjectEvent);
                            }
                        })
                        .compose(MoreObservables.filterAndMap(RxObjectEventMessage.class))
                        .compose(RxObjectEventMessage.filterAndMap(target))
                        //.compose(RxObjectEventMessage.filterAndMap(WbsMessage.class))
                        /*.filter(new Func1<WbsMessage, Boolean>() {
                    @Override
                    public Boolean call(WbsMessage dataMessage) {
                        LogHelper.getInstance().e("waitForResponseObservable " + dataMessage);

                        *//*Object respId = dataMessage.getMessageId();

                        if (respId == null) {
                            return false;
                        }
                        return respId.equals(reqId);*//*
                        return true;
                    }})*/
                        .first().timeout(7, TimeUnit.SECONDS, scheduler);

                return Observable.combineLatest(waitForResponseObservable, sendMessageObservable, new Func2<BaseDataMessage, Object, Object>() {
                    @Override
                    public Object call(BaseDataMessage baseDataMessage, Object o) {
                        return baseDataMessage;
                    }
                });
            }
        });
    }

    @NonNull
    private static Observable.Transformer<RxObjectEvent, RxObjectEventConn> isConnected() {
        return new Observable.Transformer<RxObjectEvent, RxObjectEventConn>() {
            @Override
            public Observable<RxObjectEventConn> call(Observable<RxObjectEvent> rxEventConnObservable) {
                return rxEventConnObservable.filter(new Func1<RxObjectEvent, Boolean>() {
                    @Override
                    public Boolean call(RxObjectEvent rxEventConn) {
                        if (rxEventConn instanceof RxObjectEventConn) {
                            return true;
                        }
                        return false;
                    }
                }).map(new Func1<RxObjectEvent, RxObjectEventConn>() {
                    @Override
                    public RxObjectEventConn call(RxObjectEvent rxObjectEvent) {
                        return (RxObjectEventConn) rxObjectEvent;
                    }
                });
            }
        };
    }


    private static class FilterRegisterMessage implements Func1<RxObjectEventMessage, Boolean> {
        @Override
        public Boolean call(RxObjectEventMessage rxEvent) {
            return rxEvent.message() instanceof RegisteredMessage;
        }
    }


    private class FlatMapToRegisterMessage implements Func1<RxObjectEventConnected, Observable<Object>> {

        RegisteredMessage registeredMessage;

        public FlatMapToRegisterMessage(RegisteredMessage registeredMessage) {
            this.registeredMessage = registeredMessage;
        }

        @Override
        public Observable<Object> call(RxObjectEventConnected rxEventConn) {
            /*LogHelper.getInstance().e("simulator send message for registering....");

            PublishSubject<RxObjectEvent> e = (PublishSubject<RxObjectEvent>) events;
            e.onNext(new RxObjectEventMessage(rxEventConn.sender(), registeredMessage));
            return Observable.just(new Object());*/

            //LogHelper.getInstance().e("send message for registering....");
            return Observable.just(registeredMessage)
                    .compose(RxMoreObservables.sendMessage(rxEventConn));
        }
    }
}
