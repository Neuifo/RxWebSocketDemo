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

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.exceptions.CompositeException;
import rx.exceptions.Exceptions;

public class LoggingObservables {

    @Nonnull
    public static Observer<Object> logging(@Nonnull final Logger logger, @Nonnull final String tag) {
        return new Observer<Object>() {
            @Override
            public void onCompleted() {
                logger.log(Level.INFO, tag + " - onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                logger.log(Level.SEVERE, tag + " - onError", e);
            }

            @Override
            public void onNext(Object o) {
                logger.log(Level.INFO, tag + " - onNext: {0}", o == null ? "null" : o.toString());
            }
        };
    }

    @Nonnull
    public static Observer<Object> loggingOnlyError(@Nonnull final Logger logger, @Nonnull final String tag) {
        return new Observer<Object>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                logger.log(Level.SEVERE, tag + " - onError", e);
            }

            @Override
            public void onNext(Object o) {
            }
        };
    }

    @Nonnull
    public static <T> Observable.Operator<T, T> loggingLift(@Nonnull Logger logger, @Nonnull String tag) {
        return new OperatorDoOnEach<>(logging(logger, tag));
    }

    @Nonnull
    public static <T> Observable.Operator<T, T> loggingOnlyErrorLift(@Nonnull Logger logger, @Nonnull String tag) {
        return new OperatorDoOnEach<>(loggingOnlyError(logger, tag));
    }

}

class OperatorDoOnEach<T> implements Observable.Operator<T, T> {
    private final Observer<? super T> doOnEachObserver;

    public OperatorDoOnEach(Observer<? super T> doOnEachObserver) {
        this.doOnEachObserver = doOnEachObserver;
    }

    @Override
    public Subscriber<? super T> call(final Subscriber<? super T> observer) {
        return new Subscriber<T>(observer) {

            private boolean done = false;

            @Override
            public void onCompleted() {
                if (done) {
                    return;
                }
                try {
                    doOnEachObserver.onCompleted();
                } catch (Throwable e) {
                    Exceptions.throwOrReport(e, this);
                    return;
                }
                // Set `done` here so that the error in `doOnEachObserver.onCompleted()` can be noticed by observer
                done = true;
                observer.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                // need to throwIfFatal since we swallow errors after terminated
                Exceptions.throwIfFatal(e);
                if (done) {
                    return;
                }
                done = true;
                try {
                    doOnEachObserver.onError(e);
                } catch (Throwable e2) {
                    Exceptions.throwIfFatal(e2);
                    observer.onError(new CompositeException(Arrays.asList(e, e2)));
                    return;
                }
                observer.onError(e);
            }

            @Override
            public void onNext(T value) {
                if (done) {
                    return;
                }
                try {
                    doOnEachObserver.onNext(value);
                } catch (Throwable e) {
                    Exceptions.throwOrReport(e, this, value);
                    return;
                }
                observer.onNext(value);
            }
        };
    }
}
