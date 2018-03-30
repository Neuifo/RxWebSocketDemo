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




import com.neuifo.appkit.websocket.core.object.event.RxObjectEvent;
import javax.annotation.Nonnull;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.internal.operators.OperatorMulticast;
import rx.subjects.BehaviorSubject;

public class MoreObservables {

    @Nonnull
    public static <T> Observable.Transformer<? super T, ? extends T> behaviorRefCount() {
        return (Observable.Transformer<T, T>) tObservable -> new OperatorMulticast<>(tObservable, () -> BehaviorSubject.create()).refCount();
    }

    @Nonnull
    public static <T> Observable.Transformer<Object, T> filterAndMap(@Nonnull final Class<T> clazz) {
        return observable -> observable
                .filter(o -> o != null && clazz.isInstance(o))
                .map((Func1<Object, T>) o -> {
                    //noinspection unchecked
                    return (T) o;
                });
    }

    @Nonnull
    public static Func1<Throwable, Object> throwableToIgnoreError() {
        return throwable -> new Object();
    }

    public static Observable.Operator<Object, RxObjectEvent> ignoreNext() {
        return subscriber -> new Subscriber<RxObjectEvent>(subscriber) {
            @Override
            public void onCompleted() {
                subscriber.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }

            @Override
            public void onNext(RxObjectEvent rxObjectEvent) {
                subscriber.onNext(rxObjectEvent);
            }
        };
    }
}
