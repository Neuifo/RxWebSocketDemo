package com.neuifo.appkit.model.socket;


import com.neuifo.appkit.websocket.core.model.WbsMessage;

/**
 * Created by neuifo on 2017/10/24.
 */

public enum SimpleSocketMessageType implements WbsMessage {


    TEST_MESSAGE_ONE("message desc one","socketMessageCode",0x00000000,"socketMessageBody",Object.class),
    TEST_MESSAGE_TOW("message desc two","socketMessageCode",0x00000000,"socketMessageBody",Object.class),
    TEST_MESSAGE_THREE("message desc three","socketMessageCode",0x00000000,"socketMessageBody",RegistInfo.class),
    ;

    String desc;
    String messagekey;
    Object messageValue;
    String bodyKey;
    Class messageTarget;

    SimpleSocketMessageType(String desc, String messagekey, Object messageValue, String bodyKey, Class messageTarget) {
        this.desc = desc;
        this.messagekey = messagekey;
        this.messageValue = messageValue;
        this.bodyKey = bodyKey;
        this.messageTarget = messageTarget;
    }

    @Override
    public Class getMessageTarget() {
        return messageTarget;
    }

    @Override
    public Object getMessageValue() {
        return messageValue;
    }

    @Override
    public String getMessageKey() {
        return messagekey;
    }

    @Override
    public String getBodyKey() {
        return bodyKey;
    }
}
