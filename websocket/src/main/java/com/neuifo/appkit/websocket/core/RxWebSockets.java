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


import android.support.annotation.NonNull;


import com.neuifo.appkit.websocket.core.event.RxEvent;
import com.neuifo.appkit.websocket.core.event.RxEventBinaryMessage;
import com.neuifo.appkit.websocket.core.event.RxEventConnected;
import com.neuifo.appkit.websocket.core.event.RxEventDisconnected;
import com.neuifo.appkit.websocket.core.event.RxEventPong;
import com.neuifo.appkit.websocket.core.event.RxEventStringMessage;
import com.neuifo.appkit.websocket.exp.SocketExceptionWrapper;
import java.io.IOException;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subscriptions.Subscriptions;

/**
 * This class allows to retrieve messages from websocket
 */
public class RxWebSockets {

    public static final boolean DEBUG = true;// BuildConfig.DEBUG;

    @NonNull
    private final OkHttpClient client;
    @NonNull
    private final Request request;

    /**
     * Create instance of {@link RxWebSockets}
     *
     * @param client  {@link OkHttpClient} instance
     * @param request request to connect to websocket
     */
    public RxWebSockets(@NonNull OkHttpClient client, @NonNull Request request) {
        this.client = client;
        this.request = request;
    }

    /**
     * Returns observable that connected to a websocket and returns {@link }'s
     *
     * @return Observable that connects to websocket
     */
    @NonNull
    public Observable<RxEvent> webSocketObservable() {

        return Observable.create(new Observable.OnSubscribe<RxEvent>() {

                                     private final Object lock = new Object();
                                     private WebSocket webSocketItem;
                                     private /*final*/ WebSocketCall webSocketCall;
                                     private boolean requestClose;

                                     @Override
                                     public void call(final Subscriber<? super RxEvent> subscriber) {

                                         //if (RxWebSockets.DEBUG) LogHelper.getInstance().e(subscriber.toString());

                                         /*final WebSocketCall*/
                                         webSocketCall = WebSocketCall.create(client, request);

                                         subscriber.add(Subscriptions.create(new Action0() {

                                             @Override
                                             public void call() {
                                                 // 当subscriber.unsubscribe或者subscriber.onComplete()/onError()时调用
                                                 tryToClose();
                                             }

                                             private void tryToClose() {

                                                 synchronized (lock) {
                                                     if (webSocketItem != null) {
                                                         try {
                                                             /*WebSocketProtocol

                                                             java.lang.IllegalArgumentException: Code must be in range [1000,5000): 100
                                                             at okhttp3.internal.ws.WebSocketProtocol.validateCloseCode(WebSocketProtocol.java:104)
                                                             at okhttp3.internal.ws.WebSocketWriter.writeClose(WebSocketWriter.java:101)
                                                             at okhttp3.internal.ws.RealWebSocket.close(RealWebSocket.java:168)
                                                               */
                                                             //if (DEBUG)
                                                                // LogHelper.getInstance().e(">>>>>Subscriptions man close ");
                                                             webSocketItem.close(1000, "Just disconnectPrevious");

                                                         } catch (IOException e) {
                                                             subscriber.onNext(new RxEventDisconnected(e));
                                                             subscriber.onError(e);
                                                         }
                                                         webSocketItem = null;
                                                     } else {
                                                         //if (DEBUG)
                                                             //LogHelper.getInstance().e(" Subscriptions request close");
                                                     }

                                                 }
                                                 webSocketCall.cancel();
                                             }


                                         }));

                                         // Thread.currentThread().setPriority(Process.THREAD_PRIORITY_BACKGROUND);
                                         // Thread.currentThread().setDaemon(Process.THREAD_PRIORITY_BACKGROUND);
                                         // Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                                         WebSocketListener listener = new WebSocketListener() {
                                             @Override
                                             public void onOpen(WebSocket webSocket, Response response) {
                                                 //if (DEBUG) LogHelper.getInstance().e(" onOpen");

                                                 final WebSocket notifyConnected;
                                                 synchronized (lock) {
                                                     if (requestClose) {
                                                         notifyConnected = null;
                                                         try {
                                                             //if (DEBUG) LogHelper.getInstance().e("onOpen but requestClose");
                                                             webSocket.close(100, "Just disconnectPrevious");

                                                         } catch (IOException e) {
                                                             // 上次请求取消连接

                                                             // assert subscriber.isUnsubscribed() == true
                                                             IOException wrapper = new SocketExceptionWrapper(true, "isUnsubscribed", e);
                                                             subscriber.onNext(new RxEventDisconnected(wrapper));
                                                         }
                                                     } else {
                                                         notifyConnected = new LockingWebSocket(webSocket);
                                                     }
                                                     webSocketItem = notifyConnected;
                                                 }
                                                 //if (DEBUG) LogHelper.getInstance().e("onOpen " + webSocketItem);

                                                 if (notifyConnected != null) {
                                                     // 通知已经建立连接
                                                     subscriber.onNext(new RxEventConnected(notifyConnected));
                                                 }
                                             }

                                             /*    java.net.ConnectException: Failed to connect to /211.152.46.36:9602
                                                              at okhttp3.internal.io.RealConnection.connectSocket(RealConnection.java:142)
                                                              at okhttp3.internal.io.RealConnection.connect(RealConnection.java:111)
                                                              at okhttp3.internal.http.StreamAllocation.findConnection(StreamAllocation.java:188)
                                                              at okhttp3.internal.http.StreamAllocation.findHealthyConnection(StreamAllocation.java:127)
                                                              at okhttp3.internal.http.StreamAllocation.newStream(StreamAllocation.java:97)
                                                              at okhttp3.internal.http.HttpEngine.connect(HttpEngine.java:289)
                                                              at okhttp3.internal.http.HttpEngine.sendRequest(HttpEngine.java:241)
                                                              at okhttp3.RealCall.getResponse(RealCall.java:240)
                                                              at okhttp3.RealCall$ApplicationInterceptorChain.proceed(RealCall.java:198)
                                                              at okhttp3.RealCall.getResponseWithInterceptorChain(RealCall.java:160)
                                                              at okhttp3.RealCall.access$100(RealCall.java:30)
                                                              at okhttp3.RealCall$AsyncCall.execute(RealCall.java:127)
                                                              at okhttp3.internal.NamedRunnable.run(NamedRunnable.java:32)
                                                              at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1080)
                                                              at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:573)
                                                              at java.lang.Thread.run(Thread.java:856)*/

                                             @Override
                                             public void onFailure(IOException e, Response response) {

                                                 if (response != null) {
                                                     //if (DEBUG)
                                                         //LogHelper.getInstance().e("onFailure <1>" + response.toString());
                                                 }

                                                 //if (DEBUG) LogHelper.getInstance().e("》》》》》onFailure <2>" + e);
                                                 e.printStackTrace();

                                                 returnException(e);
                                             }

                                             @Override
                                             public void onMessage(ResponseBody message) throws IOException {
                                                 try {
                                                     final WebSocket sender = webSocketOrNull();
                                                     if (sender == null) {
                                                         return;
                                                     }

                                                     if (subscriber.isUnsubscribed()) {
                                                         return;
                                                     }

                                                     // 通知已经收到消息
                                                     if (WebSocket.BINARY.equals(message.contentType())) {
                                                         byte[] orgin = message.bytes();
                                                         byte[] bytes = new byte[orgin.length - 6];
                                                         /*for (int i = 0; i < bytes.length; i++) {
                                                             bytes[i] = orgin[6 + i];
                                                         }*/
                                                         System.arraycopy(orgin,6,bytes,0,bytes.length);
                                                         subscriber.onNext(new RxEventBinaryMessage(sender, bytes));
                                                     } else if (WebSocket.TEXT.equals(message.contentType())) {
                                                         String stringMessage = message.string();
                                                         if (DEBUG) {
                                                             //LogHelper.getInstance().e("receive message " + message.toString()); // string()调用了close()
                                                             //LogHelper.getInstance().e("receive message " + stringMessage); // string()调用了close()
                                                         }
                                                         subscriber.onNext(new RxEventStringMessage(sender, stringMessage));
                                                     }
                                                 } finally {
                                                     message.close();
                                                 }
                                             }

                                             @Override
                                             public void onPong(Buffer payload) {
                                                 final WebSocket sender = webSocketOrNull();
                                                 if (sender == null) {
                                                     return;
                                                 }

                                                 //if (DEBUG) LogHelper.getInstance().e(" onPong payload: " + payload + " thread " + payload.readByteString());
                                                 if (payload == null) {
                                                     subscriber.onNext(new RxEventPong(sender, null));
                                                 } else {
                                                     subscriber.onNext(new RxEventPong(sender, payload.readByteArray()));
                                                 }


                                             }

                                             @Override
                                             public void onClose(int code, String reason) {
                                                 //if (DEBUG) LogHelper.getInstance().e(" onClose " + code + " " + reason);

                                                 returnException(new ServerRequestedCloseException(code, reason));
                                             }


                                             //////////////////////////////////////////////////////////////////////
                                             @android.support.annotation.Nullable
                                             private WebSocket webSocketOrNull() {
                                                 synchronized (lock) {
                                                     return webSocketItem;
                                                 }
                                             }

                                             private void returnException(IOException e) {
                                                 //if (DEBUG) LogHelper.getInstance().e(" returnException");

                                                 IOException wrapper = new SocketExceptionWrapper(subscriber.isUnsubscribed(), "isUnsubscribed", e);
                                                 subscriber.onNext(new RxEventDisconnected(wrapper));
                                                 subscriber.onError(e);

                                                 synchronized (lock) {
                                                     tryToClose();
                                                     webSocketItem = null;
                                                     requestClose = false;
                                                 }
                                             }

                                             private void tryToClose() {
                                                 synchronized (lock) {
                                                     if (webSocketItem != null) {
                                                         try {
                                                             //if (DEBUG) LogHelper.getInstance().e(">>>>>onException try to man close");
                                                             webSocketItem.close(1000, "Just disconnect");

                                                         } catch (IOException e) {
                                                             //if (DEBUG) LogHelper.getInstance().e(">>>>>onException man close failed, emit RxEventDisconnected");

                                                             IOException wrapper = new SocketExceptionWrapper(subscriber.isUnsubscribed(), "isUnsubscribed", e);
                                                             subscriber.onNext(new RxEventDisconnected(wrapper));
                                                             subscriber.onError(e);
                                                         }
                                                         webSocketItem = null;
                                                     } else {
                                                         //if (DEBUG) LogHelper.getInstance().e(" onException request close");
                                                         requestClose = true;
                                                     }

                                                 }
                                                 webSocketCall.cancel();
                                             }

                                             //////////////////////////////////////////////////////////////////////
                                         };
                                         webSocketCall.enqueue(listener);
                                     }
                                 }

        ).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {

            }
        });
    }


    /**
     * Class that synchronizes writes to websocket
     */
    private static class LockingWebSocket implements WebSocket {


        @NonNull
        private final WebSocket webSocket;
        private boolean isClosed;

        public LockingWebSocket(@NonNull WebSocket webSocket) {
            this.webSocket = webSocket;
        }

        @Override
        public void sendMessage(RequestBody message) throws IOException {

            synchronized (this) {
                if (isClosed) {
                    //if (DEBUG) LogHelper.getInstance().e("send message but isClosed");
                    return;
                }

                try {
                    //if (DEBUG) LogHelper.getInstance().e("send message thread");

                    webSocket.sendMessage(message);
                } catch (IOException e) {
                    //if (DEBUG) LogHelper.getInstance().e(" !!!! send message error " + e);

                    webSocket.close(1000, "sendMessage error");

                    throw e;
                } catch (IllegalStateException e) {
                    //if (DEBUG) LogHelper.getInstance().e(" !!!! send ping message " + e);
                }
            }
        }

        @Override
        public void sendPing(Buffer payload) throws IOException {
            synchronized (this) {
                if (isClosed) {
                    //if (DEBUG) LogHelper.getInstance().e("send ping but isClosed");

                    return;
                }

                try {
                    //if (DEBUG) LogHelper.getInstance().e("send ping thread" + payload.readByteString());

                    webSocket.sendPing(payload);
                } catch (IOException e) {
                    //if (DEBUG) LogHelper.getInstance().e(" !!!! send ping error " + e);

                    webSocket.close(1000, "sendPing error");
                    throw e;

                } catch (IllegalStateException e) {
                    //if (DEBUG) LogHelper.getInstance().e(" !!!! send ping error " + e);
                }
            }
        }

        /*
        java.lang.IllegalStateException: closed
        at okhttp3.internal.ws.RealWebSocket.close(RealWebSocket.java:164)
        at com.chengshi.app.dianjia.data.websocket.RxWebSockets$LockingWebSocket.close(RxWebSockets.java:329)
        */
        @Override
        public void close(final int code, final String reason) throws IOException {
            synchronized (this) {
                if (isClosed) {
                    //if (DEBUG) LogHelper.getInstance().e(" close but isClosed ");
                    return;
                }
                //if (DEBUG) LogHelper.getInstance().e("close thread " + reason);
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            webSocket.close(code, reason);
                        } catch (IllegalStateException e) {
                            //if (DEBUG) LogHelper.getInstance().e(" !!!! close error " + e);
                        } catch (IOException e) {
                            //if (DEBUG) LogHelper.getInstance().e(" !!!! close error " + e);
                            // throw e;
                        }
                        isClosed = true;
                    }
                }.start();
            }

        }
    }
}
