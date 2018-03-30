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

package com.neuifo.appkit.websocket.core.object;


import android.support.annotation.NonNull;


import com.neuifo.appkit.websocket.core.RxWebSockets;
import com.neuifo.appkit.websocket.core.event.RxEvent;
import com.neuifo.appkit.websocket.core.event.RxEventBinaryMessage;
import com.neuifo.appkit.websocket.core.event.RxEventConnected;
import com.neuifo.appkit.websocket.core.event.RxEventDisconnected;
import com.neuifo.appkit.websocket.core.event.RxEventPong;
import com.neuifo.appkit.websocket.core.event.RxEventStringMessage;
import com.neuifo.appkit.websocket.core.event.RxObjectEventPong;
import com.neuifo.appkit.websocket.core.object.event.RxObjectEvent;
import com.neuifo.appkit.websocket.core.object.event.RxObjectEventConnected;
import com.neuifo.appkit.websocket.core.object.event.RxObjectEventDisconnected;
import com.neuifo.appkit.websocket.core.object.event.RxObjectEventMessage;
import com.neuifo.appkit.websocket.core.object.event.RxObjectEventWrongStringMessageFormat;
import java.io.IOException;


import okhttp3.RequestBody;
import okhttp3.ws.WebSocket;
import okio.Buffer;
import rx.Observable;
import rx.Subscriber;

/**
 * This class allows to retrieve json messages from websocket
 */
public class RxObjectWebSockets {
    @NonNull
    private final RxWebSockets rxWebSockets;
    @NonNull
    private final ObjectSerializer objectSerializer;

    /**
     * Creates {@link RxObjectWebSockets}
     *
     * @param rxWebSockets     socket that is used to connect to server
     * @param objectSerializer that is used to parse messages
     */
    public RxObjectWebSockets(@NonNull RxWebSockets rxWebSockets, @NonNull ObjectSerializer objectSerializer) {
        this.rxWebSockets = rxWebSockets;
        this.objectSerializer = objectSerializer;
    }

    /**
     * Returns observable that connected to a websocket and returns {@link RxObjectEvent}s
     *
     * @return Observable that connects to websocket
     * @see RxWebSockets#webSocketObservable()
     */
    @NonNull
    public Observable<RxObjectEvent> webSocketObservable() {

        return rxWebSockets.webSocketObservable()

                .lift(new Observable.Operator<RxObjectEvent, RxEvent>() {

                    @Override
                    public Subscriber<? super RxEvent> call(final Subscriber<? super RxObjectEvent> subscriber) {
                        return new Subscriber<RxEvent>(subscriber) {

                            @Override
                            public void onCompleted() {
                                subscriber.onCompleted();
                            }

                            @Override
                            public void onError(Throwable e) {
                                subscriber.onError(e);
                            }

                            @Override
                            public void onNext(RxEvent rxEvent) {
                                /*if (RxWebSockets.DEBUG)
                                    LogHelper.getInstance().e(" webSocketObservable onNext " + rxEvent.toString());*/

                                if (rxEvent instanceof RxEventConnected) {
                                    subscriber.onNext(new RxObjectEventConnected(jsonSocketSender(((RxEventConnected) rxEvent).sender())));
                                } else if (rxEvent instanceof RxEventDisconnected) {
                                    subscriber.onNext(new RxObjectEventDisconnected(((RxEventDisconnected) rxEvent).exception()));
                                } else if (rxEvent instanceof RxEventStringMessage) {
                                    final RxEventStringMessage stringMessage = (RxEventStringMessage) rxEvent;
                                    subscriber.onNext(parseMessage(stringMessage));
                                } else if (rxEvent instanceof RxEventBinaryMessage) {
                                    final RxEventBinaryMessage binaryMessage = (RxEventBinaryMessage) rxEvent;
                                    subscriber.onNext(parseMessage(binaryMessage));
                                } else if (rxEvent instanceof RxEventPong) {
                                    subscriber.onNext(new RxObjectEventPong(jsonSocketSender(((RxEventPong) rxEvent).sender()), null));
                                } else {
                                    throw new RuntimeException("Unknown message type");
                                }
                            }

                            private RxObjectEvent parseMessage(RxEventStringMessage stringMessage) {
                                final String message = stringMessage.message();
                                final Object object;
                                try {
                                    object = objectSerializer.serialize(message);
                                    //LogHelper.getSystem().e("RxObjWbs",object.toString());
                                } catch (ObjectParseException e) {
                                    return new RxObjectEventWrongStringMessageFormat(jsonSocketSender(stringMessage.sender()), message, e);
                                }
                                //LogHelper.getSystem().e("RxObjWbs",object.toString());
                                return new RxObjectEventMessage(jsonSocketSender(stringMessage.sender()), object);
                            }

                            private RxObjectEvent parseMessage(RxEventBinaryMessage binaryMessage) {
                                byte[] message = binaryMessage.message();
                                Object object = null;
                                try {
                                    object = objectSerializer.serialize(new String(message));
                                    //LogHelper.getInstance().e("parseMessage:  " + object.toString());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return new RxObjectEventMessage(jsonSocketSender(binaryMessage.sender()), object);
                            }
                        };
                    }
                });
    }


    @NonNull
    private ObjectWebSocketSender jsonSocketSender(@NonNull final WebSocket sender) {

        return new ObjectWebSocketSender() {

            @Override
            public void sendObjectMessage(@NonNull Object message) throws IOException, ObjectParseException {

                //if (RxWebSockets.DEBUG)
                //    LogHelper.getInstance().e("send " + (objectSerializer.isBinary(message) ? "BINARY" : "TEXT") + " message " + objectSerializer.deserializeString(message));

                if (objectSerializer.isBinary(message)) {
                    sender.sendMessage(RequestBody.create(WebSocket.BINARY, objectSerializer.deserializeBinary(message)));
                } else {
                    sender.sendMessage(RequestBody.create(WebSocket.TEXT, objectSerializer.deserializeString(message)));
                }
            }

            @Override
            public void sendPing() throws IOException {
                sender.sendPing(new Buffer());
            }
        };
    }

    @NonNull
    public RxWebSockets getRxWebSockets() {
        return rxWebSockets;
    }
}
