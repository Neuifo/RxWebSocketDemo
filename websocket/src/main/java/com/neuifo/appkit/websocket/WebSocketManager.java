package com.neuifo.appkit.websocket;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import com.neuifo.appkit.websocket.core.RxWebSockets;
import com.neuifo.appkit.websocket.core.model.WbsMessage;
import com.neuifo.appkit.websocket.core.object.GsonObjectSerializer;
import com.neuifo.appkit.websocket.core.object.RxObjectWebSockets;
import com.neuifo.appkit.websocket.core.object.event.RxObjectEvent;
import com.neuifo.appkit.websocket.core.object.event.RxObjectEventConn;
import com.neuifo.appkit.websocket.core.object.event.RxObjectEventDisconnected;
import com.neuifo.appkit.websocket.core.object.event.RxObjectEventMessage;
import com.neuifo.appkit.websocket.exp.ImException;
import com.neuifo.appkit.websocket.exp.SocketExceptionWrapper;
import com.neuifo.appkit.websocket.model.out.BaseDataMessage;
import com.neuifo.appkit.websocket.model.out.PingMessage;
import com.neuifo.appkit.websocket.model.out.RegisteredMessage;
import com.neuifo.appkit.websocket.model.out.SocketType;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by neuifo on 2017/9/26.
 */

public class WebSocketManager {

    private Subscription pingInterval;

    public interface Listener<T> {

        public void call(T t);
    }

    public interface MessageMapper<T, P extends BaseDataMessage> {
        public T transfer(P parent, T target);
    }

    private ConcurrentMap<Object, ConcurrentLinkedQueue<Listener>> callbacks =
            new ConcurrentHashMap();

    private void emit(Object type, Object args) {
        ConcurrentLinkedQueue<Listener> callbacks = this.callbacks.get(type);
        if (callbacks != null) {
            for (Listener fn : callbacks) {
                fn.call(args);
            }
        }
    }

    private void emitAll(Object... args) {
        Set<Object> keySet = callbacks.keySet();
        for (Object key : keySet) {
            ConcurrentLinkedQueue<Listener> callbacks = this.callbacks.get(key);
            if (callbacks != null) {
                for (Listener fn : callbacks) {
                    fn.call(args);
                }
            }
        }
    }

    /**
     * Listens on the event.
     *
     * @param event event warehouseName.
     */
    private void on(Object event, Listener fn) {
        ConcurrentLinkedQueue<Listener> callbacks = this.callbacks.get(event);
        if (callbacks == null) {
            callbacks = new ConcurrentLinkedQueue();
            ConcurrentLinkedQueue<Listener> _callbacks =
                    this.callbacks.putIfAbsent(event, callbacks);
            if (_callbacks != null) {
                callbacks = _callbacks;
            }
        }
        callbacks.add(fn);
    }

    public ConcurrentLinkedQueue<Listener> has(String event) {
        return callbacks.get(event);
    }

    public void off(Object event) {
        this.callbacks.remove(event);
    }

    /**
     * Removes the listener.
     *
     * @param event an event warehouseName.
     */
    public void off(Object event, Listener fn) {
        ConcurrentLinkedQueue<Listener> callbacks = this.callbacks.get(event);
        if (callbacks != null) {
            Iterator<Listener> it = callbacks.iterator();
            while (it.hasNext()) {
                Listener internal = it.next();
                if (sameAs(fn, internal)) {
                    it.remove();
                    break;
                }
            }
        }
    }

    private void offAll() {
        Set<Object> keySet = callbacks.keySet();
        for (Object key : keySet) {
            ConcurrentLinkedQueue<Listener> callbacks = this.callbacks.remove(key);
        }
    }

    private static boolean sameAs(Listener fn, Listener internal) {
        if (fn.equals(internal)) {
            return true;
        /*} else if (internal instanceof OnceListener) {
            return fn.equals(((OnceListener) internal).fn);*/
        } else {
            return false;
        }
    }

    /**
     * 处于多一个socket共存的考虑，将manager包裹
     */
    private class WebSocketManagerWrapper {

        RegisteredMessage registeredMessage;
        PingMessage pingMessage;
        Class<? extends BaseDataMessage> aClass;
        WbsMessage[] wbsMessages;
        String url;

        public WebSocketManagerWrapper(RegisteredMessage registeredMessage, PingMessage pingMessage,
                                       WbsMessage[] wbsMessages, Class<? extends BaseDataMessage> target, String url) {
            this.registeredMessage = registeredMessage;
            this.wbsMessages = wbsMessages;
            this.pingMessage = pingMessage;
            this.url = url;
            this.aClass = target;
        }
    }

    private static Map<String, WebSocketManager> sInstance;
    public static final String EVENT_CONNECT = "connect";
    public static final String EVENT_DISCONNECT = "disconnect";

    public static final String EVENT_PACKET_RESUME = "packet_resume";
    public static final String EVENT_PACKET_DATA = "packet_data";
    public static final String EVENT_PACKET_ERROR = "packet_error";

    private Scheduler mThreadScheduler;
    private Scheduler mPostScheduler;
    private WebSocketManagerWrapper webSocketManagerWrapper;
    private Subscription mSubscribe;
    private Observable<Boolean> mConnectedObservable;
    private boolean mConnected;
    private boolean mConnecting;
    private Socket mSocket;
    private ReconnectionTask mReconnectionTask;
    //private Gson mGson;

    /**
     * 注:target是所有消息的最基本数据结构
     */
    protected WebSocketManager(RegisteredMessage registeredMessage,
                               @Nullable PingMessage pingMessage, @NonNull String url, WbsMessage[] wbsMessages,
                               Class target, @NonNull final Scheduler networkScheduler,
                               @NonNull final Scheduler uiScheduler) {

        this.mThreadScheduler = networkScheduler;
        this.mPostScheduler = uiScheduler;
        this.webSocketManagerWrapper =
                new WebSocketManagerWrapper(registeredMessage, pingMessage, wbsMessages, target, url);
    }

    public synchronized static WebSocketManager getInstance(RegisteredMessage registeredMessage,
                                                            @Nullable PingMessage pingessage, @NonNull String url, WbsMessage[] wbsMessages,
                                                            Class<? extends BaseDataMessage> target, @NonNull final Scheduler networkScheduler,
                                                            @NonNull final Scheduler uiScheduler) {

        if (url == null || url.length() == 0) {
            throw new RuntimeException("Url is invalid");
        }

        if (sInstance == null) {
            sInstance = new HashMap<>();
        }

        WebSocketManager webSocketManager = sInstance.get(url);
        if (webSocketManager == null) {
            webSocketManager =
                    new WebSocketManager(registeredMessage, pingessage, url, wbsMessages, target,
                            networkScheduler, uiScheduler);
            sInstance.put(url, webSocketManager);
        }

        return webSocketManager;
    }

    public static synchronized void uninitialize() {
        if (sInstance != null) {
            Collection<WebSocketManager> all = sInstance.values();
            for (WebSocketManager e : all) {
                e.disconnectAll();
            }
            sInstance.clear();
            sInstance = null;
        }
    }

    private Socket initialize(String url, WbsMessage[] wbsMessages,
                              Class<? extends BaseDataMessage> target, RegisteredMessage registeredMessage,
                              PingMessage pingMessage) {
        final Gson gson =
                new GsonBuilder().registerTypeAdapter(target, new Deserializer(wbsMessages, target))
                        // .registerTypeAdapter(MessageType.class, new MessageType.SerializerDeserializer())
                        .addSerializationExclusionStrategy(new ExclusionStrategy() {
                            @Override
                            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                                final Expose expose = fieldAttributes.getAnnotation(Expose.class);
                                return expose != null && !expose.serialize();
                            }

                            @Override
                            public boolean shouldSkipClass(Class<?> aClass) {
                                return false;
                            }
                        }).addDeserializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                        final Expose expose = fieldAttributes.getAnnotation(Expose.class);
                        return expose != null && !expose.deserialize();
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> aClass) {
                        return false;
                    }
                }).create();

        //mGson = gson;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(6000, TimeUnit.SECONDS);
        builder.writeTimeout(6000, TimeUnit.SECONDS);
        builder.readTimeout(6000, TimeUnit.SECONDS);

        //        try {
        //            Object[] objects;
        //            objects = HttpsHelper.loadCertificate();
        //            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) objects[0];
        //            TrustManager trustManager = (TrustManager) objects[1];
        //            //证书暂缓
        //            //builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustManager);
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //        }

        OkHttpClient client = builder.retryOnConnectionFailure(true).build();

        final RxWebSockets webSockets = new RxWebSockets(client, new Request.Builder()
                // .url("ws://192.168.1.102:8081/ws")
                .url(url)
                //.addHeader("SO_KEEPALIVE","true")
                //.addHeader("TCP_NODELAY","true")
                //.addHeader("Sec-WebSocket-Protocol", "chat")
                .build());
        final RxObjectWebSockets jsonWebSockets =
                new RxObjectWebSockets(webSockets, new GsonObjectSerializer(gson, target));
        final SocketConnection socketConnection =
                new SocketConnectionImpl(jsonWebSockets, mThreadScheduler/*Schedulers.computation()*/);

        return new Socket(socketConnection, mThreadScheduler, registeredMessage, pingMessage, target);
    }

    public synchronized void connect() {
        //ogHelper.getInstance().e("connect Connected? " + mConnected + " " + getPendingOrRunningTask());
        if (mConnected || mConnecting) {
            return;
        }

        ReconnectionTask reconnectionTask = getPendingOrRunningTask();
        if (reconnectionTask == null) {
            // AsyncTaskCompat.executeParallel()
            // AsyncTask.execute();
            startNewTask();
        } else {
            //LogHelper.getInstance().e("reconnect mConnected? " + " task is running? " + reconnectionTask.isRunning);
            if (reconnectionTask.isRunning()) {
                reconnectionTask.reset();
            } else {
                reconnectionTask.cancel();
                startNewTask();
            }
        }
    }

    public void startPing() {
        if (webSocketManagerWrapper.pingMessage != null && pingInterval == null) {
            pingInterval = mSocket.sendPingInterval(5);
        }
    }

    private void doConnect() {
        if (mConnected || mConnecting) {
            return;
        }

        disconnectPrevious();
        mConnecting = true;

        mSocket = initialize(webSocketManagerWrapper.url, webSocketManagerWrapper.wbsMessages,
                webSocketManagerWrapper.aClass, webSocketManagerWrapper.registeredMessage,
                webSocketManagerWrapper.pingMessage);

        /*if (webSocketManagerWrapper.pingMessage != null) {
            pingInterval = mSocket.sendPingInterval(30);
        }*/

        final Subscription connectionSubscription = mSocket.
                connection().subscribeOn(mThreadScheduler).subscribe(new Action1<Object>() {
            @Override
            public void call(Object o) {
                //LogHelper.getInstance().e(o.toString());
                emit(EVENT_PACKET_DATA, o);
            }
        });

        mConnectedObservable =
                mSocket.connectedAndRegistered().map(new Func1<RxObjectEvent, Boolean>() {

                    @Override
                    public Boolean call(RxObjectEvent rxJsonEventConn) {
                        //LogHelper.getInstance().e("connection rxJsonEventConn " + rxJsonEventConn);

                        if (rxJsonEventConn == null) {//发送注册消息
                            return null;
                        }

                        if (rxJsonEventConn instanceof RxObjectEventConn) {
                            //LogHelper.getInstance().e("connection connected");
                            return true;
                        } else {
                            mConnected = false;
                            mConnecting = false;
                            RxObjectEventDisconnected disconnected =
                                    (RxObjectEventDisconnected) rxJsonEventConn;
                            //LogHelper.getInstance().e("connection disconnected " + disconnected.exception());

                            IOException ioException = disconnected.exception();
                            if (ioException instanceof SocketExceptionWrapper) {
                                if (pingInterval != null) pingInterval.unsubscribe();

                                boolean isUnsubscribed =
                                        ((SocketExceptionWrapper) ioException).isUnsubscribed();
                                if (!isUnsubscribed) {
                                    // 如果Unsubscribe不再重连
                                    internalReconnect(true);
                                } else {
                                    ReconnectionTask reconnectionTask = getPendingOrRunningTask();
                                    if (reconnectionTask != null) {
                                        reconnectionTask.cancel();
                                    }
                                }
                            } else {
                                internalReconnect(true);
                            }

                            // RxObjectEventDisconnected
                            return false;
                        }
                        // return rxJsonEventConn != null;
                    }
                }).distinctUntilChanged().subscribeOn(mThreadScheduler);
        // .observeOn(mPostScheduler);
        mConnectedObservable.subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }

                mConnecting = false;

                if (aBoolean) {
                    relayMessage();
                    relayError();
                    //初始加载
                    emit(SocketType.CONNECT, true);
                    mConnected = true;
                } else {
                    mConnected = false;
                    emit(SocketType.DISCONNECT, false);
                }
            }
        });
        mSocket.events()
                .doOnNext(rxObjectEvent -> {
                    if (rxObjectEvent instanceof RxObjectEventDisconnected) {//断线重连
                        mConnected = false;
                        mConnecting = false;
                        getPendingOrRunningTask().call(false);
                        //LogHelper.getInstance().e("offline");
                    }
                })
                .compose(MoreObservables.filterAndMap(RxObjectEventMessage.class))
                .compose(RxObjectEventMessage.filterAndMap(webSocketManagerWrapper.aClass))
                .subscribeOn(mThreadScheduler)
                .subscribe((Action1<BaseDataMessage>) baseDataMessage -> {
                    emit(EVENT_PACKET_DATA, baseDataMessage);
                    //LogHelper.getInstance().e(baseDataMessage.toString());
                });

        mSubscribe = connectionSubscription;
    }

    private void disconnectPrevious() {
        if (mSubscribe != null && !mSubscribe.isUnsubscribed()) {
            mSubscribe.unsubscribe();
        }
    }

    private void disconnectAll() {
        disconnectPrevious();
        if (getPendingOrRunningTask() != null) {
            ReconnectionTask reconnectionTask = getPendingOrRunningTask();
            reconnectionTask.cancel();
        }
    }

    private synchronized ReconnectionTask getPendingOrRunningTask() {
        return mReconnectionTask;
    }

    class ReconnectionTask extends TimerTask implements Listener {

        private Timer timer;
        private int failingCount = 0;
        private boolean isRunning;
        private boolean isCancelled;

        /*public*/
        synchronized boolean isRunning() {
            return isRunning;
        }

        /*public*/
        synchronized void setRunning(boolean running) {
            isRunning = running;
        }

        /*public*/
        synchronized boolean isCancelled() {
            return isCancelled;
        }

        public ReconnectionTask() {
        }

        public ReconnectionTask(Timer timer, int failingCount) {
            this.timer = timer;
            this.failingCount = failingCount;
        }

        /*public*/
        synchronized void reset() {
            failingCount = 0;
        }

        synchronized void increaseFailingCount() {
            failingCount++;
        }

        @Override
        public synchronized boolean cancel() {
            boolean r = super.cancel();
            isCancelled = true;

            if (timer != null) {
                timer.cancel();
                timer = null;
            }

            return r;
        }

        @Override
        public void run() {
            if (isCancelled()) {
                //LogHelper.getInstance().e("ReconnectionTask try to run, but is cancelled");
                return;
            }

            if (mConnected || mConnecting) {
                //LogHelper.getInstance().e("ReconnectionTask try to run, but connected or connecting");
                return;
            }

            //LogHelper.getInstance().e("ReconnectionTask run");
            setRunning(true);
            // 不然callbacks有重复的listener
            // offAll();

            onConnect(this);
            onDisconnect(this);

            doConnect();
        }

        synchronized int getFailingCount() {
            return failingCount;
        }

        /*private*/
        synchronized int getDelayForFailingCount() {

            if (failingCount > 30) {
                return 300;
            }

            if (failingCount > 20) {
                return 100;
            }

            if (failingCount > 13) {
                return 60;
            }

            if (failingCount > 10) {
                return 30;
            }

            if (failingCount > 7) {
                return failingCount * 2;
            }

            return failingCount + 1;
        }

        public void schedule() {
            if (timer == null) {
                Timer timer = new Timer();
                this.timer = timer;
            }

            timer.schedule(this, 100);
        }

        public void scheduleDelay(long delay) {
            if (timer == null) {
                Timer timer = new Timer();
                this.timer = timer;
            }

            timer.schedule(this, delay);
        }

        @Override
        public void call(Object args) {
            Boolean r = (Boolean) args;
            if (r) {
                //LogHelper.getInstance().e("ReconnectionTask receive connect event");
                mReconnectionTask = null;
            } else {
                //LogHelper.getInstance().e("ReconnectionTask receive disconnect event " + isCancelled + " " + timer);
                increaseFailingCount();

                // RxObjectEventDisconnected
                if (!isCancelled && timer != null) {
                   /* LogHelper.getInstance()
                            .e("ReconnectionTask try to connect failingCount "
                                    + failingCount
                                    + " delay "
                                    + (getDelayForFailingCount() * 1000));*/
                    // java.lang.IllegalStateException: TimerTask is scheduled already
                    startNewTaskDelay(timer, getFailingCount(), (getDelayForFailingCount() * 1000));
                    // cancel和执行过了就不能再执行了
                    // timer.schedule(ReconnectionTask.this, getDelayForFailingCount() * 1000);
                } else {

                }
            }

            offConnect(this);
            offDisconnect(this);

            setRunning(false);
        }
    }

    private synchronized void startNewTaskDelay(Timer timer, int failingCount, long delay) {
        //LogHelper.getInstance().e("startNewTaskDelay " + failingCount);
        mReconnectionTask = new ReconnectionTask(timer, failingCount);
        mReconnectionTask.scheduleDelay(delay);
    }

    private synchronized void startNewTask() {
        //LogHelper.getInstance().e("startNewTask");
        mReconnectionTask = new ReconnectionTask();
        mReconnectionTask.schedule();
    }

    private void internalReconnect(boolean forceConnect) {
       /* LogHelper.getInstance()
                .e("internalReconnect  current Connected? "
                        + mConnected
                        + "; current task? "
                        + getPendingOrRunningTask());*/

        if (!forceConnect && (mConnected || mConnecting)) {
            return;
        }

        mConnecting = false;
        ReconnectionTask reconnectionTask = getPendingOrRunningTask();
        if (reconnectionTask == null) {
            startNewTask();
        } else {
            /*LogHelper.getInstance()
                    .e("reconnect " + " task is running? " + reconnectionTask.isRunning);*/
            /*if (reconnectionTask.isRunning) {
                reconnectionTask.reset();
            } else {
                reconnectionTask.cancel();
                startNewTask();
            }*/

        }
    }

    public void offConnect(Listener listener) {
        off(EVENT_CONNECT, listener);
    }

    public String onConnect(final Listener listener) {
        if (listener == null) {
            return null;
        }
        // String hashCode = String.valueOf(listener.hashCode());
        on(EVENT_CONNECT, listener);
        return EVENT_CONNECT;
    }

    public String onDisconnect(Listener listener) {
        if (listener == null) {
            return null;
        }
        on(EVENT_DISCONNECT, listener);
        return EVENT_DISCONNECT;
    }

    public void offDisconnect(Listener listener) {
        off(EVENT_DISCONNECT, listener);
    }

    private void relayMessage() {
        if (has(EVENT_PACKET_DATA) == null) {
            on(EVENT_PACKET_DATA, new Listener() {
                @Override
                public void call(Object args) {
                    WbsMessage socketDataResultMessages = (WbsMessage) args;
                    emit(EVENT_PACKET_DATA, socketDataResultMessages);//emit3
                    //LogHelper.getInstance().e("relayMessage{" + socketDataResultMessages.getMessageId() + "}");
                }
            });
        }
    }

    private void relayError() {
        if (has(EVENT_PACKET_ERROR) == null) {
            on(EVENT_PACKET_ERROR, new Listener() {
                @Override
                public void call(Object args) {
                    //LogHelper.getInstance().e("relayError");
                    WbsMessage errorMessage = (WbsMessage) args;
                    /*if (errorMessage.getMessageId() != null) {
                        emit(EVENT_PACKET_DATA, errorMessage);
                    } else {//TODO 这里有问题?
                    }*/
                    emitAll(errorMessage);
                }
            });
        }
    }

    class ListenerEntry implements Map.Entry<Object, Listener> {
        private Object msgId;
        private Listener listener;

        public ListenerEntry(Object msgId, Listener listener) {
            this.msgId = msgId;
            this.listener = listener;
        }

        @Override
        public Object getKey() {
            return msgId;
        }

        @Override
        public Listener getValue() {
            return listener;
        }

        @Override
        public Listener setValue(Listener object) {
            Listener old = listener;
            listener = object;
            return old;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ListenerEntry that = (ListenerEntry) o;

            if (!msgId.equals(that.msgId)) return false;
            return listener != null ? listener.equals(that.listener) : that.listener == null;
        }

        @Override
        public int hashCode() {
            int result = msgId.hashCode();
            result = 31 * result + (listener != null ? listener.hashCode() : 0);
            return result;
        }
    }

    public boolean isConnected() {
        return mConnected;
    }

    public <T> Map.Entry<Object, Listener> onMessage(final Class<T> clazz,
                                                     final Listener<T> listener) {
        return onMessage(clazz, listener, null);
    }

    public <T, P extends BaseDataMessage<T>> Map.Entry<Object, Listener> onMessage(
            final Class<T> clazz, final Listener<T> listener, final MessageMapper<T, P> messageMapper) {
        Listener wrapperListener = new Listener() {
            @Override
            public void call(Object args) {

                if (webSocketManagerWrapper.aClass.isInstance(args)) {
                    P baseDataMessage = (P) args;
                    T t = baseDataMessage.getData();
                    if (clazz.isInstance(t)) {
                        //LogHelper.getInstance().e("onMessage " + clazz.getSimpleName());
                        //这里需要rx包装？
                        Observable.just(t)
                                .zipWith(Observable.just(baseDataMessage), new Func2<T, P, T>() {
                                    @Override
                                    public T call(T t, P p) {
                                        if (messageMapper != null) {
                                            return messageMapper.transfer(p, t);
                                        }
                                        return t;
                                    }
                                })
                                .subscribeOn(mThreadScheduler)
                                .observeOn(mPostScheduler)
                                .subscribe(new Action1<T>() {
                                    @Override
                                    public void call(T t) {
                                        if (listener != null) {
                                            listener.call(t);
                                            return;
                                        }
                                    }
                                });
                    }
                }
            }
        };
        on(EVENT_PACKET_DATA, wrapperListener);
        return new ListenerEntry(clazz, wrapperListener);
    }

    public String onMessageResume(final Listener listener) {
        on(EVENT_PACKET_RESUME, new Listener() {
            @Override
            public void call(Object args) {
                //LogHelper.getInstance().e("onMessage resume");
                if (listener != null) {
                    listener.call(args);
                }
            }
        });
        return EVENT_PACKET_RESUME;
    }

    public void sendMessage(final Object wbsMessage) {
        if (!isConnected()) {
            internalReconnect(true);
            return;
        }

        //LogHelper.getInstance().e(" >>>>send message ");

        // LockSupport lockSupport = new LockSupport();
        mSocket.sendMessageOnceWhenConnected(new Func1<String, Observable<Object>>() {
            @Override
            public Observable<Object> call(String s) {

                //LogHelper.getInstance().e(">>>>sendMessageOnceWhenConnected");

                return Observable.<Object>just(wbsMessage);
            }
        }).observeOn(mPostScheduler).subscribeOn(mThreadScheduler)./*onErrorResumeNext(new Func1<Throwable, Observable<?>>() {

            @Override
            public Observable<?> call(Throwable throwable) {
                return null;
            }
        }).*/subscribe(new Subscriber<Object>() {

            @Override
            public void onCompleted() {
                //LogHelper.getInstance().e("send message success");
            }

            @Override
            public void onError(Throwable e) {
                //LogHelper.getInstance().e("send message error" + e.getLocalizedMessage());
            }

            @Override
            public void onNext(Object o) {

            }
        });
    }

    public interface OnMessageListener<T> {

        void onRequestError(ImException e);

        void onRequestSuccess(T data);
    }

    public interface DataMapper<I, O> {
        O map(I o);
    }

    /**
     * 主动请求，类似http请求
     *
     * @param object 消息体自动封装，外层包裹什么的都要自己封装
     * @param listener 监听
     */
    public <T> void sendMessage(final Object object, final OnMessageListener listener,
                                final DataMapper mapper) {
        //LogHelper.getInstance().e(" >>>>send message " + Thread.currentThread().getName());

        if (!isConnected()) {
            if (listener != null) {
                // 没有连接，直接报错返回
                listener.onRequestError(ImException.createNetworkError());
            }
            internalReconnect(true);
            return;
        }

        // LockSupport lockSupport = new LockSupport();
        mSocket.sendMessageOnceWhenConnectedV2(true, new Func1<String, Object>() {
            @Override
            public Object call(String s) {//拼接id

                return object;
            }
        }).map(new Func1<Object, Object>() {
            @Override
            public Object call(Object o) {

                if (webSocketManagerWrapper.aClass.isInstance(o)) {
                    BaseDataMessage<T> baseDataMessage = (BaseDataMessage<T>) o;
                    T t = baseDataMessage.getData();
                    if (mapper != null) {
                        return mapper.map(t);
                    }
                    return baseDataMessage.getData();
                }
                return o;
            }
        }).subscribeOn(mThreadScheduler)./*onErrorResumeNext(new Func1<Throwable, Observable<?>>() {

            @Override
            public Observable<?> call(Throwable throwable) {
                return null;
            }
        }).*/observeOn(mPostScheduler).subscribe(new SocketErrorHandler() {
            @Override
            public void onNext(Object o) {
                if (listener != null) {
                    listener.onRequestSuccess(o);
                }
                super.onNext(o);
            }

            @Override
            public void onError(Throwable e) {
                if (listener != null) {
                    listener.onRequestError(ImException.createNetworkError());
                }
                super.onError(e);
            }
        });
    }
}
