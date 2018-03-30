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


import com.neuifo.appkit.websocket.core.RxWebSockets;
import com.neuifo.appkit.websocket.core.object.RxObjectWebSockets;
import com.neuifo.appkit.websocket.core.object.event.RxObjectEvent;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;

public class SocketConnectionImpl implements SocketConnection {

    @NonNull
    private final RxObjectWebSockets sockets;
    @NonNull
    private final Scheduler scheduler;

    public SocketConnectionImpl(@NonNull RxObjectWebSockets sockets, @NonNull Scheduler scheduler) {
        this.sockets = sockets;
        this.scheduler = scheduler;
    }

    @NonNull
    public Observable<RxObjectEvent> createConnection() {
        //
        return sockets.webSocketObservable();
        //.retryWhen(repeatDuration(1, TimeUnit.SECONDS));
        //.retryWhen(maxTimes(3, 1));
    }


    private Func1<Observable<? extends Throwable>, Observable<Long>> createRetryPolicy(final int max, final int baseDelay) {

        return new Func1<Observable<? extends Throwable>, Observable<Long>>() {
            @Override
            public Observable<Long> call(final Observable<? extends Throwable> attempts) {

                return attempts.flatMap(new Func1<Throwable, Observable<Long>>() {
                    @Override
                    public Observable<Long> call(Throwable err) {
                        int delay = 2;

                        if (err instanceof RuntimeException) {
                            String msg = err.getMessage();
                            try {
                                int count = Integer.parseInt(msg);
                                delay = count * 2;

                            } catch (NumberFormatException e) {
                            }

                        }
                        //LogHelper.getInstance().e("retryWhen " + err);

                        //LogHelper.getInstance().e("delay retry by " + delay + " second(s)\n");
                        return Observable.timer(delay, TimeUnit.SECONDS);
                    }
                });

            }
        };
    }


    private Func1<Observable<? extends Throwable>, Observable<Long>> maxTimes(final int max, final int baseDelay) {

        return new Func1<Observable<? extends Throwable>, Observable<Long>>() {
            @Override
            public Observable<Long> call(Observable<? extends Throwable> attempts) {

                return attempts.zipWith(Observable.range(1, max),

                        new Func2<Throwable, Integer, Integer>() {
                            @Override
                            public Integer call(Throwable throwable, Integer i) {
                                return i;
                            }
                        }).flatMap(new Func1<Integer, Observable<Long>>() {
                    @Override
                    public Observable<Long> call(Integer i) {
                        //LogHelper.getInstance().e("delay retry by " + i + " second(s)");
                        return Observable.timer(i * baseDelay, TimeUnit.SECONDS);
                    }
                });
            }
        };
    }

    @NonNull
    private Func1<Observable<? extends Throwable>, Observable<?>> repeatDuration(final long delay, @NonNull final TimeUnit timeUnit) {
        return new Func1<Observable<? extends Throwable>, Observable<?>>() {
            @Override
            public Observable<?> call(Observable<? extends Throwable> attemps) {
                // 不能这样
                // return Observable.timer(delay, timeUnit, scheduler);

                //LogHelper.getInstance().e("receive Throwable for retrying to connect...");

                return attemps.flatMap(new Func1<Throwable, Observable<?>>() {
                    @Override
                    public Observable<?> call(Throwable err) {
                        return Observable.timer(delay, timeUnit, scheduler);
                    }
                });
            }
        };
    }

    @Nonnull
    @Override
    public Observable<RxObjectEvent> connection() {
        return sockets.webSocketObservable();
    }

    @Override
    public RxWebSockets getWebSocket() {
        return sockets.getRxWebSockets();
    }
}
