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

package com.neuifo.appkit.websocket.core.object.event;



import com.neuifo.appkit.websocket.core.object.ObjectParseException;
import com.neuifo.appkit.websocket.core.object.ObjectSerializer;
import com.neuifo.appkit.websocket.core.object.ObjectWebSocketSender;
import javax.annotation.Nonnull;

/**
 * Event indicating that string data returned by server was not correctly parsed
 *
 * This means {@link ObjectParseException} was returned via
 * {@link ObjectSerializer#deserializeString(Object)}}
 */
public class RxObjectEventWrongStringMessageFormat extends RxObjectEventWrongMessageFormat {
    @Nonnull
    private final String message;

    public RxObjectEventWrongStringMessageFormat(@Nonnull ObjectWebSocketSender sender,
                                                 @Nonnull String message,
                                                 @Nonnull ObjectParseException exception) {
        super(sender, exception);
        this.message = message;
    }

    @Nonnull
    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return "RxJsonEventWrongStringMessageFormat{" +
                "message='" + message + '\'' +
                '}';
    }
}
