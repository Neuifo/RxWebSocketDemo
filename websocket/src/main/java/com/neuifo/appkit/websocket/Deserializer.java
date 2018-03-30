package com.neuifo.appkit.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import com.neuifo.appkit.websocket.core.model.WbsMessage;
import com.neuifo.appkit.websocket.model.out.BaseDataMessage;
import java.lang.reflect.Type;

/**
 * Created by neuifo on 2017/9/22.
 */

public class Deserializer implements JsonDeserializer<Object> {

    WbsMessage[] wbsDataMessages;


    Class<? extends BaseDataMessage> targetClazz;

    Object target;

    public Deserializer(WbsMessage[] wbsDataMessages, Class<? extends BaseDataMessage> target) {
        this.targetClazz = target;
        this.wbsDataMessages = wbsDataMessages;
    }

    @Override
    public Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        target = null;

        if (wbsDataMessages == null || wbsDataMessages.length == 0) {
            throw new IllegalArgumentException("You have not regist any MessageType");
        }

        final JsonObject jsonObject = jsonElement.getAsJsonObject();

        for (WbsMessage wbsDataMessage : wbsDataMessages) {
            JsonElement messageKey = jsonObject.get(wbsDataMessage.getMessageKey());
            if (messageKey == null) {
                continue;
            }

            if (messageKey.toString().equals(wbsDataMessage.getMessageValue().toString())) {//0x10001==0x10001 or type==Event
                JsonElement bodyMessage = jsonObject.get(wbsDataMessage.getBodyKey());
                Object o = new Gson().fromJson(bodyMessage.getAsString(), wbsDataMessage.getMessageTarget());

                BaseDataMessage baseDataMessage = new Gson().fromJson(jsonObject, targetClazz);
                baseDataMessage.setData(o);
                target = baseDataMessage;
                break;
            }
        }

        if (target == null) {
            target = jsonElement.getAsJsonObject();
        }


        //LogHelper.getSystem().e("Deserializer", target.toString());
        return target;
    }
}
