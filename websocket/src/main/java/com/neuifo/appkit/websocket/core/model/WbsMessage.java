package com.neuifo.appkit.websocket.core.model;


import com.neuifo.appkit.model.socket.SimpleSocketMessageType;
import com.neuifo.appkit.websocket.Deserializer;

/**
 * Created by neuifo on 2017/9/22.
 * the base socket json data message warpper<br>
 *
 *     <p>
 *         the message i had handled,the struct always follows the rules:<br>
 *         {socketMessageCode:"10001",socketMessageBody:"{...}",}:<br>
 *         with the class {@link SimpleSocketMessageType} for data binding<br>
 *         with the class {@link Deserializer} for data decode<br>
 *         or you can override them for your own data handling
 *
 *     </p>
 *
 *
 *
 *
 */

public interface WbsMessage {

    String getMessageKey();

    String getBodyKey();

    Class getMessageTarget();

    Object getMessageValue();

}
