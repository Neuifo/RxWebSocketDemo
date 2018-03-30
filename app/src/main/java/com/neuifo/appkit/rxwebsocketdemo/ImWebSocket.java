package com.neuifo.appkit.rxwebsocketdemo;

import com.neuifo.appkit.model.socket.SimpleBaseSocketMessage;
import com.neuifo.appkit.model.socket.RegistInfo;
import com.neuifo.appkit.model.socket.SimpleSocketMessageType;
import com.neuifo.appkit.websocket.WebSocketManager;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by neuifo on 2017/10/10.
 */

public class ImWebSocket {

    private WebSocketManager instance;
    private Long currentImid;
    private String currentChatId;
    private MessageCallBack messageCallBack;

    interface MessageCallBack {
        void registSuccess();
    }


    /**
     * please notify {@link com.neuifo.appkit.websocket.Deserializer Deserializer} the data encoder
     * and {@link com.neuifo.appkit.websocket.core.model.WbsMessage WbsMessage} the data info
     * warpper </br> simple is the {@link SimpleSocketMessageType}
     */
    public void init(String ip) {

        SimpleBaseSocketMessage<RegistInfo> registMessage = new SimpleBaseSocketMessage();
        RegistInfo imRegistInfo = new RegistInfo();
        registMessage.setBody(imRegistInfo);

        SimpleBaseSocketMessage pingMessage = new SimpleBaseSocketMessage();

        /**
         * "ws://" +your ip
         */
        instance = WebSocketManager.getInstance(registMessage, pingMessage,
                ip, SimpleSocketMessageType.values(),
                SimpleBaseSocketMessage.class, Schedulers.io(), AndroidSchedulers.mainThread());

        /**
         * add listener for what you want
         */
        instance.onMessage(RegistInfo.class, new WebSocketManager.Listener<RegistInfo>() {
            @Override
            public void call(RegistInfo registInfo) {
                if (messageCallBack != null) {
                    messageCallBack.registSuccess();
                }
            }
        }, new WebSocketManager.MessageMapper<RegistInfo, SimpleBaseSocketMessage<RegistInfo>>() {
            @Override
            public RegistInfo transfer(SimpleBaseSocketMessage<RegistInfo> parent, RegistInfo target) {
                //in here ,you can warpper parent's messages
                target.sendTime = parent.getTime();
                return target;
            }
        });

        instance.connect();
    }

    public void stop() {
        if (instance != null) {
            instance.uninitialize();
        }
    }


}
