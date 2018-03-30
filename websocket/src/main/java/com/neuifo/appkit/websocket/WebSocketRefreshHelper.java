package com.neuifo.appkit.websocket;/*
package com.higgs.app.haoliecw.data.im.websocket;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import com.higgs.app.haoliecw.data.im.websocket.core.model.WbsMessage;
import com.higgs.app.haoliecw.data.utils.LogHelper;
import com.higgs.app.haoliecw.data.utils.TimeTicker;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

*/
/**
 * Created by neuifo on 2017/9/26.
 *//*


public class WebSocketRefreshHelper implements TimeTicker.OnRefreshPeriodChangedListener {

    protected WebSocketManager mWebSocketManager;
    private Map<String, WebSocketManager.Listener> mListenerMap;
    private SocketRefreshCallback mCallback;

    @Override
    public void onUpdate(long period) {
        mRefreshPeriod = period;
        if (period < 0) {
            mRefreshPeriod = DEFAULT_REFRESH_PERIOD;
        }
    }

    public interface MessageListener {
        void onReceiveMessage(Object msg);
    }

    public interface SocketRefreshCallback {

        boolean hasVisibleScreen();

        boolean isIdleAndPrepared();

        WbsMessage createMessage(String messageTypeId);

        long getRefreshPeriod(String messageTypeId);

        boolean onReceiveMessage(Object data);
    }

    private static long DEFAULT_REFRESH_PERIOD = 1500;
    private long mSendMessageDelayWhenConnected = 200L;
    private long mSendMessageDelayWhenError = 1000L;

    private long mRefreshPeriod = DEFAULT_REFRESH_PERIOD;

    private Map<String, Class> mRespMessageTypeMaps = new HashMap<>();

    private Map<String, Integer> mMessageIdsMaps = new HashMap<>();



    private void initialize() {

        mapMessageId();

        addRefreshPeriodObserver();

        setupListenerIfNecessary();
    }


    private void mapMessageId() {
        if (mRespMessageTypeMaps.size() > 0) {
            Set<String> keySet = mRespMessageTypeMaps.keySet();
            for (String s : keySet) {
                int hash = s.hashCode() * 31 + mRespMessageTypeMaps.get(s).hashCode();
                mMessageIdsMaps.put(s, Math.abs(hash));
            }
        }
    }

    private void addRefreshPeriodObserver() {
        mRefreshPeriod = TimeTicker.getInstance().getRefreshPeriod();
        if (mRefreshPeriod < 0) {
            mRefreshPeriod = DEFAULT_REFRESH_PERIOD;
        }

        TimeTicker.getInstance().addOnRefreshPeriodChangedListeners(this);
    }

    public void start() {
        start(mSendMessageDelayWhenConnected);
    }

    public void start(long delay) {
        startInternal(false, delay);
    }

    private void startInternal(boolean force, long delay) {
        LogHelper.getSystem().d( "start");
        */
/*if (mWebSocketManager == null) {
            mWebSocketManager = createDefaultEngine();
        }*//*

        // pause();
        setupListenerIfNecessary();

        if (!mWebSocketManager.isConnected()) {
            LogHelper.getSystem().d( "not connected, try to connect");
            mWebSocketManager.connect();
        } else {
            LogHelper.getSystem().d( "connected, send message delay");
            startAllDelay(force, delay, 200);
        }
    }

    private void setupListenerIfNecessary() {
        if (mListenerMap != null) {
            return;
        }

        mListenerMap = new HashMap<>();
        WebSocketManager.Listener connectedListener = createConnectedListener();
        String event = mWebSocketManager.onConnect(connectedListener);
        mListenerMap.put(event, connectedListener);

        WebSocketManager.Listener disconnectedListener = createOnDisconnectListener();
        event = mWebSocketManager.onDisconnect(disconnectedListener);
        mListenerMap.put(event, disconnectedListener);

        WebSocketManager.Listener onResumeListener = createOnResumeListener();
        event = mWebSocketManager.onMessageResume(onResumeListener);
        mListenerMap.put(event, onResumeListener);

        Map<String, Class> respMessageTypeIds = getRespMessageTypeIds();
        Set<String> ids = respMessageTypeIds.keySet();
        for (String id : ids) {
            Class respType = respMessageTypeIds.get(id);
            Map.Entry<String, WebSocketManager.Listener> entry = mWebSocketManager.onMessage(respType, createOnMessageListener(id));
            mListenerMap.put(entry.getKey(), entry.getValue());
        }
    }

    */
/**
     * 不再监听返回的消息，但是WebsocketManger还是会处理
     *//*

    public void pause() {
        if (mWebSocketManager != null) {
            List<Integer> ids = getAllHandlerId();
            for (Integer i : ids) {
                mHandler.removeMessages(i);
            }

            if (mListenerMap != null && mListenerMap.size() > 0) {
                Set<String> keySet = mListenerMap.keySet();
                for (String event : keySet) {
                    mWebSocketManager.off(event, mListenerMap.get(event));
                }
                mListenerMap.clear();
                mListenerMap = null;
            }
        }
    }


    @NonNull
    private WebSocketManager.Listener createConnectedListener() {
        return new WebSocketManager.Listener() {
            @Override
            public void call(Object... args) {

                // Toast.makeText(context, "Connected", Toast.LENGTH_LONG).show();
                startAllDelay(true, mSendMessageDelayWhenConnected, 200);
            }
        };
    }


    private void startAllDelay(boolean force, long delay, int diff) {
        long totalDelay = delay;
        List<Integer> ids = getAllHandlerId();
        for (Integer i : ids) {
            sendMessageDelayed(force, i, totalDelay);
            totalDelay = totalDelay + diff;
        }
    }

    private List<Integer> getAllHandlerId() {
        List<Integer> ids = new ArrayList<Integer>();
        Set<String> keySet = mMessageIdsMaps.keySet();
        for (String s : keySet) {

            Integer id = mMessageIdsMaps.get(s);
            ids.add(id);
        }
        return ids;
    }

    protected void sendMessageDelayed(boolean force, int handlerId, long delay) {
        if (hasVisibleScreen()) {
            boolean debug = true;
            String messageTypeId = getMessageTypeId(handlerId);

            if (!force && mHandler.hasMessages(handlerId)) {
                if (removeExistMessages(messageTypeId)) {
                    mHandler.removeMessages(handlerId);
                    mHandler.sendEmptyMessageDelayed(handlerId, delay);
                } else {

                }
            } else {
                mHandler.sendEmptyMessageDelayed(handlerId, delay);
            }
        } else {
            LogHelper.getSystem().d( "sendMessageDelayed hasVisibleScreen: false mCallback:" + mCallback);
        }
    }

    protected boolean hasVisibleScreen() {
        if (mCallback != null) {
            return mCallback.hasVisibleScreen();
        }

        return false;
    }

    private String getMessageTypeId(int handlerId) {
        Set<String> keySet = mMessageIdsMaps.keySet();
        for (String s : keySet) {
            Integer id = mMessageIdsMaps.get(s);
            if (id == handlerId) {
                return s;
            }
        }
        return null;
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (!hasVisibleScreen()) {
                LogHelper.getSystem().d( "intent to real send message, but no hasVisibleScreen mCallback:" + mCallback);
                return;
            }

            int handlerId = msg.what;
            if (!isIdleAndPrepared()) {
                WebSocketRefreshHelper.this.sendMessageDelayed(false, handlerId, 2000);
            } else {
                String messageTypeId = getMessageTypeId(handlerId);

                LogHelper.getSystem().d( "real send message for " */
/*+ ids*//*
 + messageTypeId);

                WbsMessage data = createMessage(messageTypeId);
                if (data == null) {
                    LogHelper.getSystem().d( "but data is null " */
/*+ ids*//*
);
                    return;
                }
                mWebSocketManager.sendMessage(data);
            }

        }
    };

    protected boolean isIdleAndPrepared() {
        if (mCallback != null) {
            return mCallback.isIdleAndPrepared();
        }
        return false;
    }

    protected WbsMessage createMessage(String messageTypeId) {
        if (mCallback != null) {
            WbsMessage msg = mCallback.createMessage(messageTypeId);
            return msg;
        }
        return null;
    }

    @NonNull
    private WebSocketManager.Listener createOnDisconnectListener() {
        return new WebSocketManager.Listener() {
            @Override
            public void call(Object... args) {//ToDo 暂不处理
            }
        };
    }

    @NonNull
    private WebSocketManager.Listener createOnMessageErrorListener(String messageId) {
        return new WebSocketManager.Listener() {
            @Override
            public void call(Object... args) {//ToDo 暂不处理
            }
        };
    }

    public Map<String, Class> getRespMessageTypeIds() {
        return mRespMessageTypeMaps;
    }

    @NonNull
    private WebSocketManager.Listener createOnResumeListener() {
        return new WebSocketManager.Listener() {
            @Override
            public void call(Object... args) {

                startAllDelay(true, mSendMessageDelayWhenConnected, 200);
            }
        };
    }

    protected long getRefreshPeriod(String messageTypeId) {
        if (mCallback != null) {
            long period = mCallback.getRefreshPeriod(messageTypeId);
            if (period > 0) {
                return period;
            }
        }

        if (isUseDefaultRefreshPeriod(messageTypeId)) {
            return mRefreshPeriod;
        }

        return Integer.MAX_VALUE;
    }

    protected boolean removeExistMessages(String messageTypeId) {
        return false;
    }

    protected boolean isUseDefaultRefreshPeriod(String messageTypeId) {
        return true;
    }



    protected boolean onReceiveMessage(Object msg) {
        boolean flag = false;
        if (mCallback != null) {
            flag = mCallback.onReceiveMessage(msg);
        }

        for (MessageListener e : mMessageListenerSet) {
            e.onReceiveMessage(msgTypeId, msg);
        }
        return flag;
    }


    @NonNull
    private WebSocketManager.Listener createOnMessageListener(final String messageTypeId) {
        return new WebSocketManager.Listener() {
            @Override
            public void call(Object... args) {
                boolean handled = false;

                try {
                    Object*/
/*List<InstrumentEntity>*//*
 msg = args[0];
                    // LogHelper.log(TAG, "receive message: " + msg + " delegate " + getViewDelegate());
                    handled = onReceiveMessage(messageTypeId, msg);
                    */
/*if (msg != null && msg.size() > 0) {

                    }*//*


                    */
/*boolean debug = true;
                    if ("default".equals(messageTypeId)) {
                        debug = false;
                    }*//*


                    if (handled && hasVisibleScreen()) {
                        // if (debug) LogHelper.getGao().d(TAG, "ON message, and hasVisibleScreen");
                        int handlerId = getHandlerId(messageTypeId);
                        long period = getRefreshPeriod(messageTypeId);
                        sendMessageDelayed(false, handlerId, period);
                    }
                    mErrorMap.remove(getHandlerId(messageTypeId));

                } catch (Exception e) {
                    //处理webSocket回调报错
                    LogHelper.getGao().e(TAG, "Handle Message error", e);
                    handled = onMessageFormatError();

                    if (handled) {
                        //  出错了
                        int handlerId = getHandlerId(messageTypeId);
                        long period = getRefreshPeriod(messageTypeId) + 1000;

                        Integer errorCount = mErrorMap.get(getHandlerId(messageTypeId));
                        if (errorCount == null) {
                            errorCount = 1;
                        } else {
                            errorCount += 1;
                        }
                        period = mSendMessageDelayWhenError * (int) Math.pow(2, errorCount);
                        sendMessageDelayed(false, handlerId, period);
                    }
                }

            }
        };
    }
}
*/
